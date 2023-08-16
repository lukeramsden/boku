package interview;

import org.agrona.collections.Object2ObjectHashMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class URLShortenerService
{
    private static final int ASCII_LOWERCASE_RANGE_LOWER_BOUND = 97;
    private static final int ASCII_LOWERCASE_UPPER_BOUND = 122;
    private static final String SHORT_URL_PREFIX = "http://www.short.com/";

    private final Random random;
    private final Map<String, String> shortenedUrls = new Object2ObjectHashMap<>();

    public URLShortenerService(Random random)
    {
        this.random = random;
    }

    public String shortenUrlToRandom(String longUrl) throws ShortUrlAlreadyExistsException, InvalidUrlException
    {
        if (!isValidUrl(longUrl))
        {
            throw new InvalidUrlException(longUrl);
        }

        final String randomFourCharacterString = generateRandomFourCharacterString();
        final String shortUrl = SHORT_URL_PREFIX + randomFourCharacterString;

        if (shortenedUrls.containsKey(shortUrl))
        {
            // Handling colliding randomness is hard
            throw new ShortUrlAlreadyExistsException(shortUrl);
        }

        shortenedUrls.put(shortUrl, longUrl);
        return shortUrl;
    }

    public String shortenUrlToKeyword(String longUrl, String shortKeyword) throws InvalidUrlException, ShortUrlAlreadyExistsException
    {
        if (!isValidUrl(longUrl))
        {
            throw new InvalidUrlException(longUrl);
        }

        final String shortUrl = SHORT_URL_PREFIX + shortKeyword;

        if (shortenedUrls.containsKey(shortUrl))
        {
            throw new ShortUrlAlreadyExistsException(shortUrl);
        }

        shortenedUrls.put(shortUrl, longUrl);
        return shortUrl;
    }

    public Optional<String> getOriginalUrl(String shortUrl) throws InvalidUrlException
    {
        if (!isValidUrl(shortUrl))
        {
            throw new InvalidUrlException(shortUrl);
        }

        return Optional.ofNullable(shortenedUrls.get(shortUrl));
    }

    private String generateRandomFourCharacterString()
    {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++)
        {
            stringBuilder.append(
                    (char) random.nextInt(ASCII_LOWERCASE_RANGE_LOWER_BOUND, ASCII_LOWERCASE_UPPER_BOUND + 1)
            );
        }

        return stringBuilder.toString();
    }

    private boolean isValidUrl(String longUrl)
    {
        try
        {
            new URL(longUrl);
            return true;
        } catch (MalformedURLException e)
        {
            return false;
        }
    }

    public static final class InvalidUrlException extends Exception
    {
        public InvalidUrlException(final String invalidUrl)
        {
            super("URL '%s' is not a valid URL".formatted(invalidUrl));
        }
    }

    public static final class ShortUrlAlreadyExistsException extends Exception
    {
        public ShortUrlAlreadyExistsException(String shortUrl)
        {
            super("Short URL '%s' already exists".formatted(shortUrl));
        }
    }
}
