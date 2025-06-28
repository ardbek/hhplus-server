package kr.hhplus.be.server.common.lock;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(kr.hhplus.be.server.common.lock.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = REDISSON_LOCK_PREFIX + CustomSpringElParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key); // 락 이름으로 RLock 인스턴스 가져옴.

        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()); //정의된 waitTime까지 획득 시도, leaseTime이 지나면 잠금 해제
            if(!available) {
                return false;
            }
            return aopForTransaction.proceed(joinPoint); // 별도의 트랜잭션으로 실행
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new InterruptedException("락을 기다리는 동안 인터럽트가 발생했습니다.");
        } finally {
            try{
                rLock.unlock(); // 종료 시 무조건 락 해제
            } catch(IllegalMonitorStateException e){
                log.info("Redisson Lock Already UnLock serviceName = {}, key = {}", method.getName(), key);
            }
        }

    }
}
