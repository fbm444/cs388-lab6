package com.codepath.campgrounds

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.campgrounds.databinding.ActivityMainBinding
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Headers
import org.json.JSONException

fun createJson() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

private const val TAG = "CampgroundsMain/"
private const val PREFS_NAME = "campground_prefs"
private const val CACHE_ENABLED_KEY = "cache_enabled"
private val PARKS_API_KEY = BuildConfig.API_KEY
private val CAMPGROUNDS_URL =
    "https://developer.nps.gov/api/v1/campgrounds?api_key=${PARKS_API_KEY}"

class MainActivity : AppCompatActivity() {
    private lateinit var campgroundsRecyclerView: RecyclerView
    private lateinit var binding: ActivityMainBinding
    private lateinit var campgroundAdapter: CampgroundAdapter

    private val allCampgrounds = mutableListOf<Campground>()
    private val campgrounds = mutableListOf<Campground>()
    private var searchQuery = ""
    private var isOffline = false

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }
    private val cacheEnabled get() = prefs.getBoolean(CACHE_ENABLED_KEY, true)

    private val connectivityManager by lazy { getSystemService(ConnectivityManager::class.java) }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread { setOffline(false) }
        }

        override fun onLost(network: Network) {
            runOnUiThread { setOffline(connectivityManager.activeNetwork == null) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        campgroundsRecyclerView = findViewById(R.id.campgrounds)

        campgroundAdapter = CampgroundAdapter(this, campgrounds)
        campgroundsRecyclerView.adapter = campgroundAdapter

        lifecycleScope.launch {
            (application as CampgroundApplication).db.campgroundDao().getAll().collect { databaseList ->
                if (!cacheEnabled) return@collect
                databaseList.map { entity ->
                    Campground(
                        entity.name,
                        entity.description,
                        entity.latLong,
                        listOf(CampgroundImage(entity.imageUrl, null))
                    )
                }.also { mappedList ->
                    showCampgrounds(mappedList)
                }
            }
        }

        campgroundsRecyclerView.layoutManager = LinearLayoutManager(this).also {
            val dividerItemDecoration = DividerItemDecoration(this, it.orientation)
            campgroundsRecyclerView.addItemDecoration(dividerItemDecoration)
        }

        binding.swipeRefresh.setOnRefreshListener { fetchCampgrounds() }

        setOffline(connectivityManager.activeNetwork == null)
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        fetchCampgrounds()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_cache).isChecked = cacheEnabled

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText.orEmpty()
                applyFilter()
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.action_cache) return super.onOptionsItemSelected(item)

        item.isChecked = !item.isChecked
        prefs.edit().putBoolean(CACHE_ENABLED_KEY, item.isChecked).apply()
        if (item.isChecked) {
            fetchCampgrounds()
        } else {
            lifecycleScope.launch(IO) {
                (application as CampgroundApplication).db.campgroundDao().deleteAll()
            }
        }
        return true
    }

    private fun fetchCampgrounds() {
        val client = AsyncHttpClient()
        client.get(CAMPGROUNDS_URL, object : JsonHttpResponseHandler() {
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.e(TAG, "Failed to fetch campgrounds: $statusCode")
                runOnUiThread { binding.swipeRefresh.isRefreshing = false }
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "Successfully fetched campgrounds: $json")
                binding.swipeRefresh.isRefreshing = false
                try {
                    val parsedJson = createJson().decodeFromString(
                        CampgroundResponse.serializer(),
                        json.jsonObject.toString()
                    )
                    parsedJson.data?.let { list ->
                        if (!cacheEnabled) {
                            showCampgrounds(list)
                            return
                        }
                        lifecycleScope.launch(IO) {
                            (application as CampgroundApplication).db.campgroundDao().deleteAll()
                            (application as CampgroundApplication).db.campgroundDao().insertAll(list.map {
                                CampgroundEntity(
                                    name = it.name,
                                    description = it.description,
                                    latLong = it.latLong,
                                    imageUrl = it.imageUrl
                                )
                            })
                        }
                    }

                } catch (e: JSONException) {
                    Log.e(TAG, "Exception: $e")
                }
            }

        })
    }

    private fun showCampgrounds(list: List<Campground>) {
        allCampgrounds.clear()
        allCampgrounds.addAll(list)
        applyFilter()
    }

    private fun applyFilter() {
        campgrounds.clear()
        campgrounds.addAll(allCampgrounds.filter {
            it.name.orEmpty().contains(searchQuery, ignoreCase = true)
        })
        campgroundAdapter.notifyDataSetChanged()
    }

    private fun setOffline(offline: Boolean) {
        if (offline == isOffline) return
        isOffline = offline
        binding.offlineBanner.visibility = if (offline) View.VISIBLE else View.GONE
        if (!offline) fetchCampgrounds()
    }
}
