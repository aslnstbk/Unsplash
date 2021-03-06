package com.aslnstbk.unsplash.search.presentation.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aslnstbk.unsplash.common.data.model.ProgressState
import com.aslnstbk.unsplash.common.data.model.ResponseData
import com.aslnstbk.unsplash.common.data.models.Image
import com.aslnstbk.unsplash.search.data.models.QueryHistory
import com.aslnstbk.unsplash.search.domain.SearchRepository
import com.aslnstbk.unsplash.search.presentation.models.MoreLoadingItem
import com.aslnstbk.unsplash.search.presentation.models.SearchImageItem
import com.aslnstbk.unsplash.search.presentation.models.SearchItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SearchViewModel(
    private val mainJob: Job = Job(),
    override val coroutineContext: CoroutineContext = mainJob + Dispatchers.Main,
    private val ioContext: CoroutineContext = mainJob + Dispatchers.IO,
    private val searchRepository: SearchRepository
) : ViewModel(), CoroutineScope {

    val queryHistoryLiveData: MutableLiveData<List<QueryHistory>> = MutableLiveData()
    val imagesLiveData: MutableLiveData<ResponseData<List<SearchItem>, String>> = MutableLiveData()
    val moreImagesLiveData: MutableLiveData<ResponseData<List<SearchItem>, String>> = MutableLiveData()
    val progressLiveData: MutableLiveData<ProgressState> = MutableLiveData()

    private var page = 1

    override fun onCleared() {
        super.onCleared()
        mainJob.cancel()
    }

    fun onStart() {
        getAllSearchHistory()
    }

    fun deleteSearchHistory(queryHistory: QueryHistory) = CoroutineScope(Dispatchers.IO).launch {
        searchRepository.deleteSearchHistory(queryHistory)
    }

    fun onSearchImage(query: String) = launch(coroutineContext) {
        progressLiveData.value = ProgressState.Loading

        searchRepository.searchImages(
            query = query,
            page = page,
            result = {
                imagesLiveData.value = ResponseData.Success(getImageItemsList(it.results))
                progressLiveData.value = ProgressState.Done
            },
            fail = {
                imagesLiveData.value = ResponseData.Error(it.toString())
                progressLiveData.value = ProgressState.Done
            }
        )

        addSearchHistory(query = query)
    }

    fun getMoreImages(query: String) = launch(coroutineContext) {
        page++

        searchRepository.searchImages(
            query = query,
            page = page,
            result = {
                moreImagesLiveData.value = ResponseData.Success(getImageItemsList(it.results))
            },
            fail = {
                moreImagesLiveData.value = ResponseData.Error(it.toString())
            }
        )
    }

    private fun getAllSearchHistory() = launch(ioContext) {
        queryHistoryLiveData.postValue(searchRepository.getAllSearchHistory().reversed())
    }

    private fun addSearchHistory(query: String) = launch(ioContext) {
        if (!searchRepository.checkByQuery(query)) {
            searchRepository.addSearchHistory(
                QueryHistory(query = query)
            )
        }
    }

    private fun getImageItemsList(imageList: List<Image>): List<SearchItem> {
        val searchImageItemList = imageList.map {
            SearchImageItem(
                imageId = it.id,
                imageUrl = it.urls.regular
            )
        }

        return searchImageItemList + listOf(MoreLoadingItem())
    }
}