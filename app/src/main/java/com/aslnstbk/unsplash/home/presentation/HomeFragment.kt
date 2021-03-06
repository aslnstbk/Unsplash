package com.aslnstbk.unsplash.home.presentation

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.aslnstbk.unsplash.R
import com.aslnstbk.unsplash.common.data.model.ProgressState
import com.aslnstbk.unsplash.common.data.model.ResponseData
import com.aslnstbk.unsplash.common.domain.ImageLoader
import com.aslnstbk.unsplash.common.presentation.models.ImageItem
import com.aslnstbk.unsplash.common.presentation.view.ImagesLineAdapter
import com.aslnstbk.unsplash.common.presentation.view.LoadingError
import com.aslnstbk.unsplash.common.presentation.view.ToolbarBuilder
import com.aslnstbk.unsplash.home.data.ImageClickListener
import com.aslnstbk.unsplash.home.presentation.viewmodel.HomeViewModel
import com.aslnstbk.unsplash.main.APP_ACTIVITY
import com.aslnstbk.unsplash.navigation.Screens
import com.aslnstbk.unsplash.utils.extensions.hide
import com.aslnstbk.unsplash.utils.extensions.show
import com.github.terrakok.cicerone.Router
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.fragment_home), ImageClickListener {

    private val homeViewModel: HomeViewModel by viewModel()
    private val router: Router by inject()
    private val imageLoader: ImageLoader by inject()
    private val loadingError: LoadingError by inject()

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val imagesLineAdapter: ImagesLineAdapter by lazy {
        ImagesLineAdapter(
            imageLoader = imageLoader,
            imageClickListener = this,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        buildToolbar()
        observeLiveData()
    }

    override fun onImageClick(imageId: String) {
        router.navigateTo(Screens.ImageDetails(imageId = imageId))
    }

    private fun initViews(view: View) {
        progressBar = APP_ACTIVITY.findViewById(R.id.activity_main_progress_bar)
        recyclerView = view.findViewById(R.id.fragment_home_recycler_view)
        recyclerView.adapter = imagesLineAdapter
    }

    private fun buildToolbar() {
        toolbar = ToolbarBuilder()
            .setNavigationIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_app_logo))
            .setMenu(R.menu.home_toolbar_menu)
            .build(activity = APP_ACTIVITY)

        toolbar.setOnMenuItemClickListener {
            router.navigateTo(Screens.Search())

            true
        }
    }

    private fun observeLiveData() {
        homeViewModel.imagesLiveData.observe(viewLifecycleOwner, ::handleImages)
        homeViewModel.progressLiveData.observe(viewLifecycleOwner, ::handleProgress)
    }

    private fun handleImages(responseData: ResponseData<List<ImageItem>, String>) {
        when (responseData) {
            is ResponseData.Success -> showImages(responseData.result)
            is ResponseData.Error -> showLoadingError()
        }
    }

    private fun showImages(list: List<ImageItem>) {
        imagesLineAdapter.setList(list)
        loadingError.hide()
    }

    private fun showLoadingError() {
        loadingError.show()
        loadingError.onRetryClick {
            homeViewModel.onStart()
        }
    }

    private fun handleProgress(progressState: ProgressState) {
        when (progressState) {
            ProgressState.Loading -> progressBar.show()
            ProgressState.Done -> progressBar.hide()
        }
    }
}