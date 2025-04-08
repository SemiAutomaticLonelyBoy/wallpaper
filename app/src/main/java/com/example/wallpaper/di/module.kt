package com.example.wallpaper.di

import org.koin.dsl.module
import com.example.wallpaper.ui.presentation.WallpaperList.WallpapersListViewModel
import com.example.wallpaper.data.repositories.WallpaperServiceRepository
import com.example.wallpaper.data.repositories.FileSystemRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel

val modules = module {
    viewModel { WallpapersListViewModel(get(), get()) }
    singleOf(::FileSystemRepository)
    singleOf(::WallpaperServiceRepository)
}