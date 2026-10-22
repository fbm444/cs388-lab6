package com.codepath.lab6

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.serialization.builtins.ListSerializer
import okhttp3.Headers
import org.json.JSONException

private const val TAG = "HomeFragment"
private const val API_KEY = BuildConfig.API_KEY
private const val NEWS_URL =
    "https://developer.nps.gov/api/v1/newsreleases?limit=20&api_key=${API_KEY}"
private const val NEWS_STATE_KEY = "news"

class HomeFragment : Fragment() {

    private val news = mutableListOf<NewsRelease>()
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getString(NEWS_STATE_KEY)?.let {
            news.clear()
            news.addAll(createJson().decodeFromString(ListSerializer(NewsRelease.serializer()), it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val layoutManager = LinearLayoutManager(context)
        newsRecyclerView = view.findViewById(R.id.news)
        newsRecyclerView.layoutManager = layoutManager
        newsRecyclerView.setHasFixedSize(true)
        newsRecyclerView.addItemDecoration(
            DividerItemDecoration(view.context, layoutManager.orientation)
        )
        newsAdapter = NewsAdapter(view.context, news)
        newsRecyclerView.adapter = newsAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (news.isEmpty()) fetchNews()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            NEWS_STATE_KEY,
            createJson().encodeToString(ListSerializer(NewsRelease.serializer()), news)
        )
    }

    private fun fetchNews() {
        val client = AsyncHttpClient()
        client.get(NEWS_URL, object : JsonHttpResponseHandler() {
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.e(TAG, "Failed to fetch news: $statusCode")
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "Successfully fetched news: $json")
                try {
                    val parsedJson = createJson().decodeFromString(
                        NewsReleaseResponse.serializer(),
                        json.jsonObject.toString()
                    )
                    parsedJson.data?.let { list ->
                        news.clear()
                        news.addAll(list)
                        newsAdapter.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Exception: $e")
                }
            }
        })
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
