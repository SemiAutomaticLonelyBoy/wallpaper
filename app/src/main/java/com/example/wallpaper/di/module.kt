package com.example.wallpaper.di

import com.example.wallpaper.data.repositories.FileSystemRepository
import com.example.wallpaper.data.repositories.LogsRepository
import com.example.wallpaper.data.repositories.WallpaperServiceRepository
import com.example.wallpaper.ui.presentation.wallpaper_list.WallpapersListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val modules = module {
    viewModel { WallpapersListViewModel(get(), get(), get()) }
    singleOf(::FileSystemRepository)
    singleOf(::WallpaperServiceRepository)
    singleOf(::LogsRepository)
}