package mixit.web.handler

import mixit.model.Favorite
import mixit.repository.FavoriteRepository
import mixit.util.json
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.toMono

@Controller
class FavoriteHandler(private val favoriteRepository: FavoriteRepository) {

    fun toggleFavorite(req: ServerRequest) = ServerResponse.ok().json().body(req.session().flatMap { session ->
        val login = session.getAttribute<String>("login")
        val talkId = req.pathVariable("talkId")

        // We have to be authenticated to be able to change his favorites
        favoriteRepository.findByTalkAndUser(login!!, talkId)
                // if favorite is found we delete it
                .flatMap {
                    favoriteRepository.delete(login, talkId).map { FavoriteDto(talkId, false) }
                }
                // otherwise we create it
                .switchIfEmpty(favoriteRepository.save(Favorite(login, talkId)).map { FavoriteDto(it.talkId, true) })
    })

    fun getFavorite(req: ServerRequest) = ServerResponse.ok().json().body(req.session().flatMap { session ->
        val login = session.getAttribute<String>("login")
        val talkId = req.pathVariable("talkId")

        // We have to be authenticated to be able to change his favorites
        favoriteRepository.findByTalkAndUser(login!!, talkId)
                .flatMap {
                    FavoriteDto(it.talkId, true).toMono()
                }
                .switchIfEmpty(FavoriteDto(talkId, false).toMono())
    })
}

class FavoriteDto(
        val talkId: String,
        val selected: Boolean
)
