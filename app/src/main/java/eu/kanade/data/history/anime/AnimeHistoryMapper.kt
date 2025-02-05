package eu.kanade.data.history.anime

import eu.kanade.domain.entries.anime.model.AnimeCover
import eu.kanade.domain.history.anime.model.AnimeHistory
import eu.kanade.domain.history.anime.model.AnimeHistoryWithRelations
import java.util.Date

val animeHistoryMapper: (Long, Long, Date?) -> AnimeHistory = { id, episodeId, seenAt ->
    AnimeHistory(
        id = id,
        episodeId = episodeId,
        seenAt = seenAt,
    )
}

val animeHistoryWithRelationsMapper: (Long, Long, Long, String, String?, Long, Boolean, Long, Float, Date?) -> AnimeHistoryWithRelations = {
        historyId, animeId, episodeId, title, thumbnailUrl, sourceId, isFavorite, coverLastModified, episodeNumber, seenAt ->
    AnimeHistoryWithRelations(
        id = historyId,
        episodeId = episodeId,
        animeId = animeId,
        title = title,
        episodeNumber = episodeNumber,
        seenAt = seenAt,
        coverData = AnimeCover(
            animeId = animeId,
            sourceId = sourceId,
            isAnimeFavorite = isFavorite,
            url = thumbnailUrl,
            lastModified = coverLastModified,
        ),
    )
}
