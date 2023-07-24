package net.robinfriedli.aiode.function.modes;

import java.util.concurrent.Callable;

import net.robinfriedli.exec.AbstractNestedModeWrapper;
import net.robinfriedli.exec.Mode;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;

/**
 * Mode that runs the given task with default Spotify credentials applied
 */
public class SpotifyAuthorizationMode extends AbstractNestedModeWrapper {

    private final SpotifyApi spotifyApi;

    public SpotifyAuthorizationMode(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    @Override
    public <E> @NotNull Callable<E> wrap(@NotNull Callable<E> callable) {
        return () -> {
            String prevAccessToken = spotifyApi.getAccessToken();
            try {
                ClientCredentials credentials = spotifyApi.clientCredentials().build().execute();
                spotifyApi.setAccessToken(credentials.getAccessToken());

                return callable.call();
            } finally {
                spotifyApi.setAccessToken(prevAccessToken);
            }
        };
    }

    public Mode getMode(SpotifyApi spotifyApi) {
        return Mode.create().with(new SpotifyAuthorizationMode(spotifyApi));
    }

}
