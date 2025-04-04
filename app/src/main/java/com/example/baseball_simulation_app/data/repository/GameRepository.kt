package com.example.baseball_simulation_app.data.repository

import com.example.baseball_simulation_app.data.model.Game

class GameRepository {

    fun getGamesByMonth(year: Int, month: Int): List<Game> {
        // 여기서는 임시 데이터를 사용하고 있음. 실제로는 백엔드에서 불러오도록 구현할 예정.
        return listOf(
            Game(
                id = 1,
                homeTeamName = "LG",
                homeTeamLogoUrl = "https://www.lg.co.kr/media/release/images/19033",
                awayTeamName = "KT",
                awayTeamLogoUrl = "https://upload.wikimedia.org/wikipedia/ko/7/7b/Kt_%EC%9C%84%EC%A6%88_%EB%A1%9C%EA%B3%A0.png?20161114114023",
                score = "3 - 1",
                stadium = "잠실",
                date = "2024. 04. 02",
            ),
            Game(
                id = 2,
                homeTeamName = "LG",
                homeTeamLogoUrl = "https://www.lg.co.kr/media/release/images/19033",
                awayTeamName = "KT",
                awayTeamLogoUrl = "https://upload.wikimedia.org/wikipedia/ko/7/7b/Kt_%EC%9C%84%EC%A6%88_%EB%A1%9C%EA%B3%A0.png?20161114114023",
                score = "3 - 1",
                stadium = "잠실",
                date = "2024. 04. 02"
            ),
            Game(
                id = 3,
                homeTeamName = "LG",
                homeTeamLogoUrl = "https://www.lg.co.kr/media/release/images/19033",
                awayTeamName = "KT",
                awayTeamLogoUrl = "https://upload.wikimedia.org/wikipedia/ko/7/7b/Kt_%EC%9C%84%EC%A6%88_%EB%A1%9C%EA%B3%A0.png?20161114114023",
                score = "3 - 1",
                stadium = "잠실",
                date = "2024. 04. 02"
            )
        )
    }
}
