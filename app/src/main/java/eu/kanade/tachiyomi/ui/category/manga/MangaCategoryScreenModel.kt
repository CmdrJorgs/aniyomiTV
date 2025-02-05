package eu.kanade.tachiyomi.ui.category.manga

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import eu.kanade.domain.category.manga.interactor.CreateMangaCategoryWithName
import eu.kanade.domain.category.manga.interactor.DeleteMangaCategory
import eu.kanade.domain.category.manga.interactor.GetMangaCategories
import eu.kanade.domain.category.manga.interactor.RenameMangaCategory
import eu.kanade.domain.category.manga.interactor.ReorderMangaCategory
import eu.kanade.domain.category.model.Category
import eu.kanade.tachiyomi.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MangaCategoryScreenModel(
    private val getCategories: GetMangaCategories = Injekt.get(),
    private val createCategoryWithName: CreateMangaCategoryWithName = Injekt.get(),
    private val deleteCategory: DeleteMangaCategory = Injekt.get(),
    private val reorderCategory: ReorderMangaCategory = Injekt.get(),
    private val renameCategory: RenameMangaCategory = Injekt.get(),
) : StateScreenModel<MangaCategoryScreenState>(MangaCategoryScreenState.Loading) {

    private val _events: Channel<MangaCategoryEvent> = Channel()
    val events = _events.consumeAsFlow()

    init {
        coroutineScope.launch {
            getCategories.subscribe()
                .collectLatest { categories ->
                    mutableState.update {
                        MangaCategoryScreenState.Success(
                            categories = categories.filterNot(Category::isSystemCategory),
                        )
                    }
                }
        }
    }

    fun createCategory(name: String) {
        coroutineScope.launch {
            when (createCategoryWithName.await(name)) {
                is CreateMangaCategoryWithName.Result.InternalError -> _events.send(MangaCategoryEvent.InternalError)
                CreateMangaCategoryWithName.Result.NameAlreadyExistsError -> _events.send(MangaCategoryEvent.CategoryWithNameAlreadyExists)
                else -> {}
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        coroutineScope.launch {
            when (deleteCategory.await(categoryId = categoryId)) {
                is DeleteMangaCategory.Result.InternalError -> _events.send(MangaCategoryEvent.InternalError)
                else -> {}
            }
        }
    }

    fun moveUp(category: Category) {
        coroutineScope.launch {
            when (reorderCategory.moveUp(category)) {
                is ReorderMangaCategory.Result.InternalError -> _events.send(MangaCategoryEvent.InternalError)
                else -> {}
            }
        }
    }

    fun moveDown(category: Category) {
        coroutineScope.launch {
            when (reorderCategory.moveDown(category)) {
                is ReorderMangaCategory.Result.InternalError -> _events.send(MangaCategoryEvent.InternalError)
                else -> {}
            }
        }
    }

    fun renameCategory(category: Category, name: String) {
        coroutineScope.launch {
            when (renameCategory.await(category, name)) {
                is RenameMangaCategory.Result.InternalError -> _events.send(MangaCategoryEvent.InternalError)
                RenameMangaCategory.Result.NameAlreadyExistsError -> _events.send(MangaCategoryEvent.CategoryWithNameAlreadyExists)
                else -> {}
            }
        }
    }

    fun showDialog(dialog: MangaCategoryDialog) {
        mutableState.update {
            when (it) {
                MangaCategoryScreenState.Loading -> it
                is MangaCategoryScreenState.Success -> it.copy(dialog = dialog)
            }
        }
    }

    fun dismissDialog() {
        mutableState.update {
            when (it) {
                MangaCategoryScreenState.Loading -> it
                is MangaCategoryScreenState.Success -> it.copy(dialog = null)
            }
        }
    }
}

sealed class MangaCategoryDialog {
    object Create : MangaCategoryDialog()
    data class Rename(val category: Category) : MangaCategoryDialog()
    data class Delete(val category: Category) : MangaCategoryDialog()
}

sealed class MangaCategoryEvent {
    sealed class LocalizedMessage(@StringRes val stringRes: Int) : MangaCategoryEvent()
    object CategoryWithNameAlreadyExists : LocalizedMessage(R.string.error_category_exists)
    object InternalError : LocalizedMessage(R.string.internal_error)
}

sealed class MangaCategoryScreenState {

    @Immutable
    object Loading : MangaCategoryScreenState()

    @Immutable
    data class Success(
        val categories: List<Category>,
        val dialog: MangaCategoryDialog? = null,
    ) : MangaCategoryScreenState() {

        val isEmpty: Boolean
            get() = categories.isEmpty()
    }
}
