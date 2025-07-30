package org.example.Metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(value = "management.metrics.export.prometheus.enabled", havingValue = "true")
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(timed)")
    public Object timeMethod(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        String metricName = timed.value().isEmpty() ?
                "inventario_" + joinPoint.getSignature().getName() + "_tiempo" :
                timed.value();

        Timer timer = Timer.builder(metricName)
                .description(timed.description())
                .register(meterRegistry);

        return timer.recordCallable(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
    }

    @Around("@annotation(counted)")
    public Object countMethod(ProceedingJoinPoint joinPoint, Counted counted) throws Throwable {
        try {
            Object result = joinPoint.proceed();

            String metricName = counted.value().isEmpty() ?
                    "inventario_" + joinPoint.getSignature().getName() + "_total" :
                    counted.value();

            Counter.builder(metricName)
                    .description(counted.description())
                    .tags(counted.tags())
                    .register(meterRegistry)
                    .increment();

            return result;
        } catch (Exception e) {
            // Contar errores por separado
            Counter.builder("inventario_errores_total")
                    .tag("metodo", joinPoint.getSignature().getName())
                    .tag("excepcion", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            throw e;
        }
    }
}