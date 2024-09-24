import io.seruco.encoding.base62.Base62
import org.apache.commons.codec.binary.Base64
import spock.lang.Specification

import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class EncodingBenchmarkTest extends Specification {

    def base62 = Base62.createInstance()

    def "Base64와 Base62의 URL 안전성 비교 테스트"() {
        given: "테스트할 문자열 준비"
        def testStrings = [
                "This is a test string that will produce padding",  // 패딩이 필요한 문자열
                "This is another test string without padding"       // 패딩이 필요 없는 문자열
        ]

        testStrings.each { testString ->
            println "\n테스트 문자열: $testString"

            when: "Base64 인코딩 수행"
            def base64Encoded = Base64.encodeBase64String(testString.getBytes("UTF-8"))
            def urlEncodedBase64 = URLEncoder.encode(base64Encoded, "UTF-8")
            def decodedFromUrlEncodedBase64 = new String(Base64.decodeBase64(URLDecoder.decode(urlEncodedBase64, "UTF-8")), "UTF-8")

            then: "Base64 결과 확인"
            println "Base64 인코딩 결과: $base64Encoded"
            println "URL 인코딩된 Base64: $urlEncodedBase64"
            println "URL 디코딩 후 Base64 디코딩 결과: $decodedFromUrlEncodedBase64"

            base64Encoded.count('=') <= 2  // 패딩은 0, 1, 또는 2개 가능
            urlEncodedBase64.count("%3D") == base64Encoded.count('=')
            decodedFromUrlEncodedBase64 == testString

            when: "Base62 인코딩 수행"
            def base62Encoded = new String(base62.encode(testString.getBytes("UTF-8")))
            def urlEncodedBase62 = URLEncoder.encode(base62Encoded, "UTF-8")
            def decodedFromUrlEncodedBase62 = new String(base62.decode(URLDecoder.decode(urlEncodedBase62, "UTF-8").getBytes()), "UTF-8")

            then: "Base62 결과 확인"
            println "Base62 인코딩 결과: $base62Encoded"
            println "URL 인코딩된 Base62: $urlEncodedBase62"
            println "URL 디코딩 후 Base62 디코딩 결과: $decodedFromUrlEncodedBase62"

            !base62Encoded.contains('=')
            !base62Encoded.contains('+')
            !base62Encoded.contains('/')
            urlEncodedBase62 == base62Encoded  // URL 인코딩이 필요 없음
            decodedFromUrlEncodedBase62 == testString

            when: "Base64에서 패딩 제거"
            def base64EncodedNoPadding = base64Encoded.replaceAll("=+\$", "")
            def decodedNoPaddingBase64 = new String(Base64.decodeBase64(base64EncodedNoPadding), "UTF-8")

            then: "Base64 패딩 제거 결과 확인"
            println "패딩이 제거된 Base64: $base64EncodedNoPadding"
            println "패딩 제거 후 Base64 디코딩 결과: $decodedNoPaddingBase64"

            decodedNoPaddingBase64 == testString  // 패딩이 없어도 디코딩은 가능할 수 있음

            when: "Base62에서 문자 제거 시도"
            def base62EncodedModified = base62Encoded.substring(0, base62Encoded.length() - 1)

            then: "Base62 문자 제거 시 예외 발생 또는 디코딩 실패 확인"
            try {
                def decodedModifiedBase62 = new String(base62.decode(base62EncodedModified.getBytes()), "UTF-8")
                assert decodedModifiedBase62 != testString, "Base62 디코딩 결과가 원본과 달라야 함"
            } catch (Exception e) {
                println "Base62 문자 제거 시 예외 발생: ${e.message}"
            }
        }
    }

    def "실제 주소 인코딩/디코딩 테스트 (Base62와 Base64 비교)"() {
        given:
        def addresses = [
                "서울특별시 강남구 테헤란로 152",
                "123 Main St, New York, NY 10001",
                "부산광역시 해운대구 우동 센텀2로 25",
                "Paris, Champs-Élysées, 75008",
                "東京都渋谷区神南1丁目 19-11",
                "서울시 중구 을지로 66 #123",
                "Test+/=Special?Characters&In@URL"  // URL에서 문제가 될 수 있는 특수 문자 포함
        ]

        when:
        addresses.each { address ->
            def base62Encoded = new String(base62.encode(address.getBytes("UTF-8")))
            def base62Decoded = new String(base62.decode(base62Encoded.getBytes("UTF-8")), "UTF-8")

            def base64Encoded = Base64.encodeBase64String(address.getBytes("UTF-8"))  // 일반 Base64 사용
            def base64Decoded = new String(Base64.decodeBase64(base64Encoded), "UTF-8")

            println "Original: $address"
            println "Base62 Encoded: $base62Encoded"
            println "Base62 Decoded: $base62Decoded"
            println "Base64 Encoded: $base64Encoded"
            println "Base64 Decoded: $base64Decoded"
            println "Base62 URL safe: ${isUrlSafe(base62Encoded)}"
            println "Base64 URL safe: ${isUrlSafe(base64Encoded)}"
            println "---"

            assert address == base62Decoded
            assert address == base64Decoded
            assert isUrlSafe(base62Encoded)
        }

        then:
        true
    }

    def "Base62와 Base64 인코딩 성능 및 결과 길이 비교"() {
        given:
        def inputSizes = [10, 100, 1000, 10000]

        when:
        inputSizes.each { size ->
            println "입력 크기: $size 바이트"
            def input = generateRandomString(size)
            def iterations = size <= 1000 ? 1000 : 10

            def (base62Time, base62Length) = benchmark("Base62", iterations) {
                new String(base62.encode(input.getBytes()))
            }

            def (base64Time, base64Length) = benchmark("Base64", iterations) {
                Base64.encodeBase64String(input.getBytes())
            }

            println "Base62: ${base62Time}ms, 길이: ${base62Length}"
            println "Base64: ${base64Time}ms, 길이: ${base64Length}"
            println "---"
        }

        then:
        true
    }

    private String generateRandomString(int length) {
        def chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        def random = new SecureRandom()
        random.with {
            (1..length).collect { chars[nextInt(chars.size())] }.join()
        }
    }

    private def benchmark(String name, int iterations, Closure task) {
        def start = System.nanoTime()
        def result = null
        iterations.times {
            result = task()
        }
        def end = System.nanoTime()
        def time = TimeUnit.NANOSECONDS.toMillis(end - start)
        def length = result.length()
        [time, length]
    }

    private boolean isUrlSafe(String encoded) {
        return !encoded.contains('+') && !encoded.contains('/') && !encoded.contains('=')
    }
}