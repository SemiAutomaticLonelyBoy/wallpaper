package com.example.wallpaper.ui.presentation.wallpaper_list

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wallpaper.R
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallPapersListScreen(

) {
    val viewModel: WallpapersListViewModel = koinViewModel()
    val state: WallPapersListScreenState = viewModel.state.collectAsState().value

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uri: List<Uri> ->
            viewModel.inputAction(
                WallpapersListViewModel.InputAction.LoadVideoFromStorage(
                    uri
                )
            )
        },
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Плейлист")
                },
                navigationIcon = {
                    when (state) {
                        is WallPapersListScreenState.WallPapers -> {
                            if (state.isEditModeActive) {
                                IconButton(
                                    onClick = {
                                        viewModel.inputAction(
                                            WallpapersListViewModel.InputAction.SetEditableMode(
                                                false
                                            )
                                        )
                                    },
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(end = 8.dp),
                                        painter = painterResource(R.drawable.baseline_close_24),
                                        contentDescription = null,
                                    )
                                }
                            }
                        }

                        else -> Unit
                    }
                },
                actions = {
                    when (state) {
                        is WallPapersListScreenState.WallPapers -> {
                            if (state.isEditModeActive) {
                                IconButton(
                                    onClick = {
                                        viewModel.inputAction(WallpapersListViewModel.InputAction.DeleteVideos)
                                    },
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(end = 8.dp),
                                        painter = painterResource(R.drawable.baseline_delete_24),
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        pickVideoLauncher.launch("video/*")
                                    },
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(end = 8.dp),
                                        painter = painterResource(R.drawable.baseline_add_24),
                                        contentDescription = null,
                                    )
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            )
        },
    ) { innerPadding ->

        when (state) {
            WallPapersListScreenState.EmptyState -> EmptyState(
                pickVideoLauncher = pickVideoLauncher,
            )

            WallPapersListScreenState.LoadingState -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }

            is WallPapersListScreenState.WallPapers -> {
                WallPapers(
                    state = state,
                    sendAction = viewModel::inputAction,
                    paddingValues = innerPadding,
                )
            }
        }
    }
}

@Composable
private fun WallPapers(
    state: WallPapersListScreenState.WallPapers,
    sendAction: (WallpapersListViewModel.InputAction) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            itemsIndexed(
                items = state.videos,
                key = { index, _ -> state.videos[index].name },
            ) { index, item ->

                VideoItem(
                    videoName = item.name,
                    isContains = state.selectedVideos.contains(item.name),
                    state = state,
                    sendAction = sendAction,
                )
                if (index != state.videos.lastIndex){
                    HorizontalDivider()
                }
            }

        }
    }
}

@Composable
private fun VideoItem(
    videoName: String,
    isContains: Boolean,
    state: WallPapersListScreenState.WallPapers,
    sendAction: (WallpapersListViewModel.InputAction) -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .pointerInput(state.selectedVideos) {
                detectTapGestures(
                    onLongPress = {
                        if (state.isEditModeActive) return@detectTapGestures
                        sendAction(WallpapersListViewModel.InputAction.SetEditableMode(true))
                        sendAction(WallpapersListViewModel.InputAction.OnVideoClick(videoName))
                    },
                    onTap = {
                        if (state.isEditModeActive.not()) return@detectTapGestures
                        sendAction(WallpapersListViewModel.InputAction.OnVideoClick(videoName))
                    },
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(state.isEditModeActive) {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp, top = 6.dp, bottom = 6.dp),
                painter = if (isContains) {
                    painterResource(R.drawable.baseline_check_circle_24)
                } else {
                    painterResource(R.drawable.baseline_radio_button_unchecked_24)
                },
                contentDescription = null,
            )
        }
        Text(
            modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
            overflow = TextOverflow.Ellipsis,
            text = videoName,
        )
    }
}

@Composable
private fun EmptyState(
    pickVideoLauncher: ManagedActivityResultLauncher<String, List<Uri>>,
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                pickVideoLauncher.launch("video/*")
            },
        ) {
            Text("Выбрать видео")
        }
    }
}
