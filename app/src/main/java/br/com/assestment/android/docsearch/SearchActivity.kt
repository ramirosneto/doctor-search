package br.com.assestment.android.docsearch

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import br.com.assestment.android.docsearch.auth.AccountAuthenticator
import br.com.assestment.android.docsearch.model.doctor.dto.DoctorDTO
import br.com.assestment.android.docsearch.model.doctor.dto.SearchDoctor
import br.com.assestment.android.docsearch.services.ServicesHelper
import br.com.assestment.android.docsearch.services.`interface`.DoctorServicesInterface
import br.com.socialbank.android.socialpartner.custom.picasso.PicassoWithHeader
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private val REQUEST_CODE_ASK_PERMISSIONS = 123

    private lateinit var mAdapter: SearchAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    private var mIsLoading = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mQuerySearch: String? = null
    private var mLastKey: String? = null
    private var mLastKnowLat: Double = 0.0
    private var mLastKnowLng: Double = 0.0

    companion object {
        fun intentToShow(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mAdapter = SearchAdapter()
        mLayoutManager = LinearLayoutManager(applicationContext)
        recycler_view.adapter = mAdapter
        recycler_view.addOnScrollListener(setOnScrollListener())
        recycler_view.layoutManager = mLayoutManager
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mQuerySearch = query
                callSearchService(false)
                hideSoftInputKeyboard()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }
        })

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    override fun onStart() {
        checkLocationPermissions()
        super.onStart()
    }

    private fun setOnScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = mLayoutManager.childCount
                val pastVisibleItem = mLayoutManager.findFirstCompletelyVisibleItemPosition()
                val total = mAdapter.itemCount

                if (!mIsLoading) {
                    if ((visibleItemCount + pastVisibleItem) >= total) {
                        callSearchService(true)
                    }
                }

                super.onScrolled(recyclerView, dx, dy)
            }
        }
    }

    private fun getAccessToken(): String {
        val account = Account(getString(R.string.app_name), AccountAuthenticator.ACCOUNT_TYPE)
        val accountManager = AccountManager.get(applicationContext)
        return accountManager.peekAuthToken(account, AccountAuthenticator.TOKEN_TYPE_ACCESS)
    }

    private fun callSearchService(isAdded: Boolean) {
        if (mLastKnowLat == 0.0 || mLastKnowLng == 0.0) {
            Toast.makeText(applicationContext, R.string.retrieving_location, Toast.LENGTH_LONG).show()
            getLastLocation()
        } else {
            progress_bar.visibility = View.VISIBLE
            mIsLoading = true

            val authorization = "Bearer ".plus(getAccessToken())
            val retrofitClient = ServicesHelper.client(false)
            val endpoint = retrofitClient?.create(DoctorServicesInterface::class.java)
            val callback = endpoint?.search(
                mQuerySearch, mLastKnowLat, mLastKnowLng, mLastKey, authorization
            )

            callback?.enqueue(object : Callback<SearchDoctor?> {
                override fun onResponse(call: Call<SearchDoctor?>, response: Response<SearchDoctor?>) {
                    progress_bar.visibility = View.GONE
                    mIsLoading = false

                    if (response.isSuccessful) {
                        mLastKey = response.body()?.lastKey
                        handleSearchSuccess(isAdded, response.body())
                    } else {
                        createDeslogatorDialog()
                    }
                }

                override fun onFailure(call: Call<SearchDoctor?>, t: Throwable) {
                    t.printStackTrace()
                    progress_bar.visibility = View.GONE
                    recycler_view.visibility = View.GONE
                    empty_view.visibility = View.VISIBLE
                    mIsLoading = false
                }
            })
        }
    }

    private fun handleSearchSuccess(isAdded: Boolean, response: SearchDoctor?) {
        if (response != null) {
            if (response.doctors.size > 0) {
                recycler_view.visibility = View.VISIBLE
                empty_view.visibility = View.GONE

                if (isAdded) {
                    mAdapter.addData(response.doctors)
                } else {
                    mAdapter.setList(response.doctors)
                }
            } else {
                if (!isAdded) {
                    recycler_view.visibility = View.GONE
                    empty_view.visibility = View.VISIBLE
                }
            }
        } else {
            recycler_view.visibility = View.GONE
            empty_view.visibility = View.VISIBLE
        }
    }

    private fun createDeslogatorDialog() {
        AlertDialog.Builder(this@SearchActivity)
            .setTitle(R.string.deslogator_dialog_title)
            .setMessage(R.string.deslogator_dialog_message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok,
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        removeAccount()
                    }
                })
            .create()
            .show()
    }

    protected fun hideSoftInputKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun checkLocationPermissions() {
        val coarsePermission =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        val finePermission =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)

        if (coarsePermission != PackageManager.PERMISSION_GRANTED || finePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@SearchActivity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_ASK_PERMISSIONS
            )
        } else {
            getLastLocation()
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@SearchActivity)
        mFusedLocationClient?.lastLocation?.addOnSuccessListener(this@SearchActivity)
        { location: Location? ->
            location?.apply {
                mLastKnowLat = location.latitude
                mLastKnowLng = location.longitude
            }
        }
    }

    @SuppressLint("NewApi")
    private fun removeAccount() {
        val accountType = AccountAuthenticator.ACCOUNT_TYPE
        val am = AccountManager.get(applicationContext)
        val dsAccount = am.getAccountsByType(accountType)[0]

        am.removeAccount(
            dsAccount, this@SearchActivity, { accountManagerFuture ->
                if (accountManagerFuture.isDone) {
                    startActivity(LoginActivity.intentToShowClearTask(applicationContext))
                }
            }, null
        )
    }

    //inner class region

    inner class SearchAdapter() :
        RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
        var mList: ArrayList<DoctorDTO>

        init {
            mList = arrayListOf()
        }

        fun setList(list: ArrayList<DoctorDTO>) {
            mList = list
            notifyDataSetChanged()
        }

        fun addData(list: ArrayList<DoctorDTO>) {
            mList.addAll(list)
            notifyDataSetChanged()
        }

        fun remove(card: DoctorDTO) {
            mList.remove(card)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
            val inflater = LayoutInflater.from(applicationContext)
            val root = inflater.inflate(R.layout.item_search, parent, false)
            return SearchViewHolder(root)
        }

        override fun getItemCount(): Int {
            return mList.size
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            val doctor = mList.get(position)
            holder.nameTextView.name.text = doctor.name
            holder.addressTextView.address.text = doctor.address

            if (!TextUtils.isEmpty(doctor.photoId)) {
                val photoUrl = "${ServicesHelper.API_BASE_URL}doctors/${doctor.id}/keys/profilepictures"

                PicassoWithHeader.get(applicationContext)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .fit()
                    .centerCrop()
                    .into(holder.photoImageView)
            } else {
                holder.photoImageView.setImageResource(R.drawable.ic_avatar)
            }
        }

        inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val photoImageView = view.findViewById<ImageView>(R.id.photo)
            val nameTextView = view.findViewById<TextView>(R.id.name)
            val addressTextView = view.findViewById<TextView>(R.id.address)
        }
    }

    //endregion
}