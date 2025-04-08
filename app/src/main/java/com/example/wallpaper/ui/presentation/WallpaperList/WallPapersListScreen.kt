package com.example.wallpaper.ui.presentation.WallpaperList

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wallpaper.R
import com.example.wallpaper.data.repositories.FileSystemRepository
import com.example.wallpaper.service.VideoWallpaperService
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
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
            viewModel.inputAction(WallpapersListViewModel.InputAction.LoadVideoFromStorage(
                uri
            ))
        },
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Плейлист")
                },
                actions = {
                    when(state) {
                        is WallPapersListScreenState.WallPapers -> {
                            IconButton(
                                onClick = {
                                    pickVideoLauncher.launch("video/*")
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = 8.dp),
                                    painter = painterResource(R.drawable.baseline_add_24),
                                    contentDescription = null
                                )
                            }
                        }
                        else -> Unit
                    }
                }
            )
        },
    ) { innerPadding ->

        when(state) {
            WallPapersListScreenState.EmptyState -> EmptyState(
                sendAction = viewModel::inputAction,
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
fun WallPapers(
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
                key = { index, _ -> state.videos[index] },
            ) { _, item ->


                Text(
                    text = item.name,

                    )
            }
        }
    }
}

@Composable
fun EmptyState(
    sendAction: (WallpapersListViewModel.InputAction) -> Unit,
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
