package br.com.assestment.android.docsearch

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import br.com.assestment.android.docsearch.auth.AccountAuthenticator
import br.com.assestment.android.docsearch.model.doctor.dto.DoctorDTO
import br.com.assestment.android.docsearch.model.doctor.dto.SearchDoctor
import br.com.assestment.android.docsearch.services.ServicesHelper
import br.com.assestment.android.docsearch.services.`interface`.SearchDoctorServiceInterface
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private lateinit var mAdapter: SearchAdapter
    private var loading = false

    companion object {
        fun intentToShow(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mAdapter = SearchAdapter()

        recycler_view.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    if (!loading && linearLayoutManager!!.itemCount <= linearLayoutManager.findLastVisibleItemPosition() + 2) {
                        loading = true
                        //mAdapter.addData(getServiceData())
                    }
                }
            }
        )

        callSearchService(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchItem.expandActionView()

        val searchView = searchItem.actionView as SearchView
        return true
    }

    private fun callSearchService(isAdded: Boolean) {
        val account = Account(getString(R.string.app_name), AccountAuthenticator.ACCOUNT_TYPE)
        val accountManager = AccountManager.get(applicationContext)
        val accessToken = accountManager.peekAuthToken(account, AccountAuthenticator.TOKEN_TYPE_ACCESS)

        val authorization = "Bearer ".plus(accessToken)
        val retrofitClient = ServicesHelper.client(false)
        val endpoint = retrofitClient?.create(SearchDoctorServiceInterface::class.java)
        val callback = endpoint?.searchDoctor(
            "Frau",
            52.534709, 13.3976972, authorization
        )

        callback?.enqueue(object : Callback<SearchDoctor?> {
            override fun onFailure(call: Call<SearchDoctor?>, t: Throwable) {
                //
            }

            override fun onResponse(call: Call<SearchDoctor?>, response: Response<SearchDoctor?>) {
                handleSearchSuccess(isAdded, response.body())
            }
        })
    }

    private fun handleSearchSuccess(isAdded: Boolean, response: SearchDoctor?) {
        if (response != null) {
            if (isAdded) {
                mAdapter.addData(response.doctors)
            } else {
                mAdapter.setList(response.doctors)
            }
        }
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
            holder.bind(doctor)
        }

        inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val binding = view

            fun bind(doctor: DoctorDTO) {
                binding.name.text = doctor.name
                binding.address.text = doctor.address
            }
        }
    }

    //endregion
}