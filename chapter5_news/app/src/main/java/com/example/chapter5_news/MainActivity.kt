package com.example.chapter5_news

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chapter5_news.databinding.ActivityMainBinding
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var newsAdapter: NewsAdapter

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://news.google.com/")
        .addConverterFactory(
            TikXmlConverterFactory.create(
                TikXml.Builder()
                    .exceptionOnUnreadXml(false)
                    .build()
            )
        ).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        newsAdapter = NewsAdapter { url ->
            startActivity(
                Intent(this, WebViewActivity::class.java).apply {
                    putExtra("url", url)
                }
            )
        }

        val newsService = retrofit.create(NewsService::class.java)

        binding.newsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newsAdapter
        }

        binding.feedChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.feedChip.isChecked = true

            newsService.mainFeed().submitList()
        }

        binding.politicsChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.politicsChip.isChecked = true

            newsService.politicsNews().submitList()
        }

        binding.economyChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.economyChip.isChecked = true

            newsService.economyNews().submitList()
        }

        binding.socialChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.socialChip.isChecked = true

            newsService.societyNews().submitList()
        }

        binding.itChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.itChip.isChecked = true

            newsService.itNews().submitList()
        }

        binding.sportChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.sportChip.isChecked = true

            newsService.sportNews().submitList()
        }

        binding.searchTextInputEditText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.chipGroup.clearCheck()

                binding.searchTextInputEditText.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(textView.windowToken, 0)

                newsService.search(binding.searchTextInputEditText.text.toString()).submitList()

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        binding.feedChip.isChecked = true
        newsService.mainFeed().submitList()
    }

    private fun Call<NewsRss>.submitList() {
        enqueue(object : Callback<NewsRss> {
            override fun onResponse(call: Call<NewsRss>, response: Response<NewsRss>) {
                Log.d("MainActivity", "${response.body()?.channel?.items}")

                val list = response.body()?.channel?.items.orEmpty().transform()
                newsAdapter.submitList(list)

                binding.notFoundView.isVisible = list.isEmpty()

                list.forEachIndexed { index, news ->
                    Thread {
                        try {
                            val jsoup = Jsoup.connect(news.link).get()
                            val elements = jsoup.select("meta[property^=og:]")
                            val ogImageNode = elements.find { node ->
                                node.attr("property") == "og:image"
                            }

                            Log.d("imageUrl", ogImageNode?.attr("content").toString())
                            news.imageUrl = ogImageNode?.attr("content")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        runOnUiThread {
                            newsAdapter.notifyItemChanged(index)
                        }
                    }.start()
                }
            }

            override fun onFailure(call: Call<NewsRss>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }
}