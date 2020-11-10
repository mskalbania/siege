package com.reactive.siege.service;

import com.reactive.siege.model.SiegeResult;
import com.reactive.siege.model.SiegeStateContainer;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

@Service
public class SiegeService {

    private static final Logger LOG = LoggerFactory.getLogger(SiegeService.class);

    private final WebClient webClient = WebClient.create();
    private final SiegeStateContainer state = new SiegeStateContainer();

    private Disposable pendingSiege;

    public boolean start(String url, Integer tps) {
        if (!state.isRunning()) {
            try {
                pendingSiege = Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                        .subscribe(seriesNumber -> Flux.range(0, tps)
                                .parallel()
                                .runOn(Schedulers.elastic())
                                .flatMap(callNumber -> call(url))
                                .subscribe(this::handleRs, logError())
                        );
                LOG.info("Siege started @ {} | @ {} tps", url, tps);
                state.started();
                return true;
            } catch (Exception e) {
                LOG.error("Attempt to start siege failed", e);
                return false;
            }
        } else {
            LOG.warn("Siege already running");
            return false;
        }
    }

    public Option<SiegeResult> stop() {
        if (state.isRunning()) {
            try {
                pendingSiege.dispose();
            } catch (Exception e) {
                LOG.error("Error occurred while stopping siege", e);
            } finally {
                LOG.info("Siege stopped");
                state.stopped();
            }
            return Option.of(state.toSiegeResult());
        } else {
            LOG.warn("Siege not started");
            return Option.none();
        }
    }

    private Mono<ResponseEntity<String>> call(String url) {
        LOG.info("Performing call @ {}", url);
        state.incrementTransactionCount();
        return webClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String.class);
    }

    private Consumer<Throwable> logError() {
        return e -> LOG.error("Error", e);
    }

    private void handleRs(ResponseEntity<String> responseEntity) {
        LOG.debug("Server responded with: {}", responseEntity.getStatusCode());
        state.incrementVolume(responseEntity.getBody());
    }
}
