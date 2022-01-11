package nl.bress.tournamentplanner.data.factory;

import nl.bress.tournamentplanner.data.retrofit.RetrofitClient;
import nl.bress.tournamentplanner.data.services.IAuth;
import nl.bress.tournamentplanner.data.services.IGame;
import nl.bress.tournamentplanner.data.services.IPlayer;
import nl.bress.tournamentplanner.data.services.ISkillLevel;

public class ServiceFactory {

    public static IAuth createAuthService() {
        return RetrofitClient.getRetrofit().create(IAuth.class);
    }

    public static IGame createGameService(String token) {
        return RetrofitClient.getRetrofit(token).create(IGame.class);
    }

    public static IPlayer createPlayerService(String token) {
        return RetrofitClient.getRetrofit(token).create(IPlayer.class);

    }

    public static ISkillLevel createSkillLevelService(String token) {
        return RetrofitClient.getRetrofit(token).create(ISkillLevel.class);

    }
}
