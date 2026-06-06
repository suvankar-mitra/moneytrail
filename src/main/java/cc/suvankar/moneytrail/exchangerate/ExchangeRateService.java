package cc.suvankar.moneytrail.exchangerate;

import cc.suvankar.moneytrail.exception.ExchangeRateFetchException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class ExchangeRateService {
  private final ExchangeRateRepository repository;
  private final RestClient restClient;

  public ExchangeRateService(ExchangeRateRepository repository, RestClient restClient) {
    this.repository = repository;
    this.restClient = restClient;
  }

  public ExchangeRate getExchangeRate(LocalDate date, String fromCurrency, String toCurrency) {
    Optional<ExchangeRate> rateOptional =
        repository.findById(new ExchangeRateId(date, fromCurrency, toCurrency));

    if (rateOptional.isPresent()) {
      log.info(
          "Exchange rate for date {} and {} -> {} is present in DB.",
          date,
          fromCurrency,
          toCurrency);
      return rateOptional.get();
    } else {
      log.info(
          "Exchange rate is not present in DB for date {} and {} -> {}.",
          date,
          fromCurrency,
          toCurrency);
      // Fetch exchange rate
      try {
        log.info(
            "Fetching Exchange rate for date {} and {} -> {} using primary URL.",
            date,
            fromCurrency,
            toCurrency);

        String exchangeRateUrl =
            String.format(
                "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@%s/v1/currencies/%s.json",
                date.toString(), fromCurrency);

        log.info(exchangeRateUrl);

        return fetchAndSaveExchangeRate(exchangeRateUrl, date, fromCurrency, toCurrency);
      } catch (Exception ex) {
        try {
          log.warn(
              "Exception occurred while fetching Exchange rate from Primary URL: {}",
              ex.getMessage());
          log.warn("Trying with Secondary URL.");

          String exchangeRateUrl =
              String.format(
                  "https://%s.currency-api.pages.dev/v1/currencies/%s.json",
                  date.toString(), fromCurrency);

          log.info(exchangeRateUrl);

          return fetchAndSaveExchangeRate(exchangeRateUrl, date, fromCurrency, toCurrency);

        } catch (Exception e) {
          log.warn(
              "Exception occurred while fetching Exchange rate from Secondary URL: {}",
              e.getMessage());
        }
      }
    }

    log.warn("Error while fetching exchange rates for {} to {}.", fromCurrency, toCurrency);
    // throw exception exchange rate could not be fetched - user can enter it
    // manually or try again
    throw new ExchangeRateFetchException(
        "Error while fetching exchange rates for " + fromCurrency + " to " + toCurrency);
  }

  @SuppressWarnings("unchecked")
  private ExchangeRate fetchAndSaveExchangeRate(
      final String exchangeRateUrl,
      final LocalDate date,
      final String fromCurrency,
      final String toCurrency) {

    Map<String, Object> response =
        restClient
            .get()
            .uri(exchangeRateUrl)
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    Map<String, Double> currencyMap = (Map<String, Double>) response.get(fromCurrency);
    Double rate = currencyMap.get(toCurrency);

    if (rate == null) {
      throw new ExchangeRateFetchException("Currency not supported: " + toCurrency);
    }

    ExchangeRate exchangeRate = new ExchangeRate();
    exchangeRate.setExchangeRateId(new ExchangeRateId(date, fromCurrency, toCurrency));
    exchangeRate.setExchangeRate(BigDecimal.valueOf(rate));
    exchangeRate.setSource(exchangeRateUrl);
    exchangeRate.setFetchedAt(OffsetDateTime.now());

    // Persist in database
    repository.save(exchangeRate);

    log.info(
        "Found new Exchange rate from URL '{}' for date {} and {} -> {} : {}, returning.",
        exchangeRateUrl,
        date,
        fromCurrency,
        toCurrency,
        rate);

    return exchangeRate;
  }
}
