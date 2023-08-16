package interview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class URLShortenerServiceTest
{
    private static final String ORIGINAL_LONG_URL = "http://www.looooooooooooooooong.com";

    Random random = new Random();
    URLShortenerService service = new URLShortenerService(random);

    @BeforeEach
    void setSeedOfRandom()
    {
        random.setSeed(1234L);
    }

    @Test
    void shouldShortenUrlToKeyword() throws URLShortenerService.InvalidUrlException, URLShortenerService.ShortUrlAlreadyExistsException
    {
        // When
        final String shortUrl = service.shortenUrlToKeyword(ORIGINAL_LONG_URL, "keyword");

        // Then
        assertThat(shortUrl).isEqualTo("http://www.short.com/keyword");
    }

    @Test
    void shouldShortenUrlToRandomFourCharacterString() throws URLShortenerService.ShortUrlAlreadyExistsException, URLShortenerService.InvalidUrlException
    {
        // When
        final String shortUrl = service.shortenUrlToRandom(ORIGINAL_LONG_URL);

        // Then
        assertThat(shortUrl).isEqualTo("http://www.short.com/kxty");
    }

    @Test
    void shouldThrowExceptionWhenRandomnessCollides() throws URLShortenerService.ShortUrlAlreadyExistsException, URLShortenerService.InvalidUrlException
    {
        // Given
        service.shortenUrlToRandom(ORIGINAL_LONG_URL);
        random.setSeed(1234L);

        // When
        assertThatThrownBy(() -> service.shortenUrlToRandom(ORIGINAL_LONG_URL)).isInstanceOf(URLShortenerService.ShortUrlAlreadyExistsException.class);
    }

    @Test
    void shouldThrowAnExceptionIfKeywordIsUsedTwice() throws URLShortenerService.InvalidUrlException, URLShortenerService.ShortUrlAlreadyExistsException
    {
        // When
        service.shortenUrlToKeyword(ORIGINAL_LONG_URL, "keyword");

        // Then
        assertThatThrownBy(() -> service.shortenUrlToKeyword(ORIGINAL_LONG_URL, "keyword")).isInstanceOf(URLShortenerService.ShortUrlAlreadyExistsException.class);
    }

    @Test
    void shouldRetrieveOriginalUrlFromShortenedUrl() throws URLShortenerService.InvalidUrlException, URLShortenerService.ShortUrlAlreadyExistsException
    {
        // Given
        final String shortUrl = service.shortenUrlToKeyword(ORIGINAL_LONG_URL, "keyword");

        // When
        final Optional<String> originalUrl = service.getOriginalUrl(shortUrl);

        // Then
        assertThat(originalUrl.get()).isEqualTo(ORIGINAL_LONG_URL);
    }

    @Test
    void shouldReturnNothingWhenAttemptingToRetrieveByShortUrlThatDoesNotExist() throws URLShortenerService.InvalidUrlException
    {
        // Given
        // Nothing

        // When
        final Optional<String> originalUrl = service.getOriginalUrl("http://www.short.com/doesntexist");

        // Then
        assertThat(originalUrl.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    void shouldRejectInvalidLongUrlsWhenAttemptingToShortenToKeyword(final String invalidUrl)
    {
        assertThatThrownBy(() -> service.shortenUrlToKeyword(invalidUrl, "invalid")).isInstanceOf(URLShortenerService.InvalidUrlException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    void shouldRejectInvalidLongUrlsWhenAttemptingToShortenToRandom(final String invalidUrl)
    {
        assertThatThrownBy(() -> service.shortenUrlToRandom(invalidUrl)).isInstanceOf(URLShortenerService.InvalidUrlException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    void shouldRejectInvalidShortUrlsWhenAttemptingToRetrieveOriginalUrl(final String invalidUrl)
    {
        assertThatThrownBy(() -> service.getOriginalUrl(invalidUrl)).isInstanceOf(URLShortenerService.InvalidUrlException.class);
    }

    public static Stream<Arguments> invalidUrls()
    {
        return Stream.of(
                Arguments.of("abc"),
                Arguments.of("123"),
                Arguments.of("www.long.com"),
                Arguments.of("www.long"),
                Arguments.of("www.long.")
        );
    }
}
