package eu.kanade.domain.entries.anime.interactor

import eu.kanade.domain.entries.anime.model.Anime
import eu.kanade.domain.entries.anime.repository.AnimeRepository

class NetworkToLocalAnime(
    private val animeRepository: AnimeRepository,
) {

    suspend fun await(anime: Anime): Anime {
        val localAnime = getAnime(anime.url, anime.source)
        return when {
            localAnime == null -> {
                val id = insertAnime(anime)
                anime.copy(id = id!!)
            }
            !localAnime.favorite -> {
                // if the anime isn't a favorite, set its display title from source
                // if it later becomes a favorite, updated title will go to db
                localAnime.copy(title = anime.title)
            }
            else -> {
                localAnime
            }
        }
    }

    private suspend fun getAnime(url: String, sourceId: Long): Anime? {
        return animeRepository.getAnimeByUrlAndSourceId(url, sourceId)
    }

    private suspend fun insertAnime(anime: Anime): Long? {
        return animeRepository.insertAnime(anime)
    }
}
