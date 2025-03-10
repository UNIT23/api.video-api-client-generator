package video.api.client.api.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import video.api.client.api.ApiException;
import video.api.client.api.models.Caption;
import video.api.client.api.models.CaptionsUpdatePayload;
import video.api.client.api.models.Page;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * API tests for CaptionsApi
 */
@DisplayName("CaptionsApi")
public class CaptionsApiTest extends AbstractApiTest {

    private final CaptionsApi api = apiClientMock.captions();

    @Nested
    @DisplayName("delete")
    class delete {
        @Test
        @DisplayName("required parameters")
        public void requiredParametersTest() {
            answerOnAnyRequest(201, "{}");

            assertThatThrownBy(() -> api.delete(null, "en")).isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'videoId' when calling delete");

            assertThatThrownBy(() -> api.delete("12", null)).isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'language' when calling delete");

            assertThatNoException().isThrownBy(() -> api.delete("vi4k0jvEUuaTdRAEjQ4Prklgc", "en"));
        }

        @Test
        @DisplayName("204 response")
        public void responseWithStatus204Test() throws ApiException {
            answerOnAnyRequest(204, "");

            api.delete("vi4k0jvEUuaTdRAEjQ4Prklgc", "en");

        }

        @Test
        @DisplayName("404 response")
        public void responseWithStatus404Test() throws ApiException {
            answerOnAnyRequest(404, "");

            assertThatThrownBy(() -> api.delete("vi4k0jvEUuaTdRAEjQ4Prklgc", "en")).isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(404)).hasMessage("");

        }
    }

    @Nested
    @DisplayName("list")
    class list {
        private static final String PAYLOADS_PATH = "/payloads/captions/list/";

        @Test
        @DisplayName("required parameters")
        public void requiredParametersTest() {
            answerOnAnyRequest(201, "{}");

            assertThatNoException().isThrownBy(() -> api.list("vi4k0jvEUuaTdRAEjQ4Prklg").execute());
        }

        @Test
        @DisplayName("200 response")
        public void responseWithStatus200Test() throws ApiException {
            answerOnAnyRequest(200, readResourceFile(PAYLOADS_PATH + "responses/200.json"));

            Page<Caption> res = api.list("vi4k0jvEUuaTdRAEjQ4Prklg").execute();

            assertThat(res.getCurrentPage()).isEqualTo(1);
            assertThat(res.getPageSize()).isEqualTo(25);
            assertThat(res.getPagesTotal()).isEqualTo(1);
            assertThat(res.getCurrentPageItems()).isEqualTo(2);

            assertThat(res.getItems()).hasSize(2);

            assertThat(res.getItems()).containsExactlyInAnyOrder(
                    new Caption().src("https://cdn.api.video/vod/vi3N6cDinStg3oBbN79GklWS/captions/en.vtt")
                            .uri("/videos/vi3N6cDinStg3oBbN79GklWS/captions/en").srclang("en").languageName("English")._default(false),
                    new Caption().src("https://cdn.api.video/vod/vi3N6cDinStg3oBbN79GklWS/captions/fr.vtt")
                            .uri("/videos/vi3N6cDinStg3oBbN79GklWS/captions/fr").srclang("fr").languageName("Française")._default(false));
        }

        @Test
        @DisplayName("404 response")
        public void responseWithStatus404Test() throws ApiException {
            answerOnAnyRequest(404, "");

            assertThatThrownBy(() -> api.list("vi4k0jvEUuaTdRAEjQ4Prklg").execute()).isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(404)).hasMessage("");
        }
    }

    @Nested
    @DisplayName("get")
    class get {
        private static final String PAYLOADS_PATH = "/payloads/captions/get/";

        @Test
        @DisplayName("required parameters")
        public void requiredParametersTest() {
            answerOnAnyRequest(201, "{}");

            assertThatThrownBy(() -> api.get(null, "en")).isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'videoId' when calling get");

            assertThatThrownBy(() -> api.get("12", null)).isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'language' when calling get");

            assertThatNoException().isThrownBy(() -> api.get("vi4k0jvEUuaTdRAEjQ4Prklg", "en"));
        }

        @Test
        @DisplayName("200 response")
        public void responseWithStatus200Test() throws ApiException {
            answerOnAnyRequest(200, readResourceFile(PAYLOADS_PATH + "responses/200.json"));

            Caption res = api.get("vi4k0jvEUuaTdRAEjQ4Prklg", "en");

            assertThat(res.getUri()).isEqualTo("/videos/vi3N6cDinStg3oBbN79GklWS/captions/en");
            assertThat(res.getSrc()).isEqualTo("https://cdn.api.video/vod/vi3N6cDinStg3oBbN79GklWS/captions/en.vtt");
            assertThat(res.getSrclang()).isEqualTo("en");
            assertThat(res.getLanguageName()).isEqualTo("English");
            assertThat(res.getDefault()).isEqualTo(false);
        }

        @Test
        @DisplayName("404 response")
        public void responseWithStatus404Test() throws ApiException {
            answerOnAnyRequest(404, "");

            assertThatThrownBy(() -> api.get("vi4k0jvEUuaTdRAEjQ4Prklg", "en")).isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(404)).hasMessage("");
        }
    }

    @Nested
    @DisplayName("update")
    class update {
        private static final String PAYLOADS_PATH = "/payloads/captions/update/";

        @Test
        @DisplayName("required parameters")
        public void requiredParametersTest() {
            answerOnAnyRequest(201, "{}");

            assertThatThrownBy(() -> api.update(null, "en", new CaptionsUpdatePayload()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'videoId' when calling update");

            assertThatThrownBy(() -> api.update("12", null, new CaptionsUpdatePayload()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'language' when calling update");

            assertThatThrownBy(() -> api.update("12", "en", null)).isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'captionsUpdatePayload' when calling update");

            assertThatNoException()
                    .isThrownBy(() -> api.update("vi4k0jvEUuaTdRAEjQ4Prklg", "en", new CaptionsUpdatePayload()));
        }

        @Test
        @DisplayName("200 response")
        public void responseWithStatus200Test() throws ApiException {
            answerOnAnyRequest(200, readResourceFile(PAYLOADS_PATH + "responses/200.json"));

            Caption res = api.update("vi4k0jvEUuaTdRAEjQ4Prklg", "en", new CaptionsUpdatePayload());

            assertThat(res.getUri()).isEqualTo("/videos/vi3N6cDinStg3oBbN79GklWS/captions/en");
            assertThat(res.getSrc()).isEqualTo("https://cdn.api.video/vod/vi3N6cDinStg3oBbN79GklWS/captions/en.vtt");
            assertThat(res.getSrclang()).isEqualTo("en");
            assertThat(res.getLanguageName()).isEqualTo("English");
            assertThat(res.getDefault()).isEqualTo(true);
        }

        @Test
        @DisplayName("400-0 response")
        public void responseWithStatus400Test() throws ApiException {
            answerOnAnyRequest(400, readResourceFile(PAYLOADS_PATH + "responses/400-0.json"));

            assertThatThrownBy(() -> api.update("vi4k0jvEUuaTdRAEjQ4Prklg", "en_", new CaptionsUpdatePayload()))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(400))
                    .hasMessage("An attribute is invalid.");
        }

        @Test
        @DisplayName("404 response")
        public void responseWithStatus404Test() throws ApiException {
            answerOnAnyRequest(404, readResourceFile(PAYLOADS_PATH + "responses/404.json"));

            assertThatThrownBy(() -> api.update("vi4k0jvEUuaTdRAEjQ4Prklg", "en", new CaptionsUpdatePayload()))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(404))
                    .hasMessage("sunt do fugiat tempor");
        }
    }

    @Nested
    @DisplayName("upload")
    class upload {
        private static final String PAYLOADS_PATH = "/payloads/captions/upload/";

        @Test
        @DisplayName("required parameters")
        public void requiredParametersTest() {
            answerOnAnyRequest(201, "{}");

            assertThatThrownBy(() -> api.upload(null, "en", new File(""))).isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'videoId' when calling upload");

            assertThatThrownBy(() -> api.upload("12", null, new File(""))).isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'language' when calling upload");

            assertThatThrownBy(() -> api.upload("12", "en", null)).isInstanceOf(ApiException.class)
                    .hasMessage("Missing the required parameter 'file' when calling upload");

            assertThatNoException().isThrownBy(() -> api.upload("vi4k0jvEUuaTdRAEjQ4Prklg", "en", new File("")));
        }

        @Test
        @DisplayName("200 response")
        public void responseWithStatus200Test() throws ApiException {
            answerOnAnyRequest(200, readResourceFile(PAYLOADS_PATH + "responses/200.json"));

            Caption res = api.upload("vi4k0jvEUuaTdRAEjQ4Prklg", "en", new File(""));

            assertThat(res.getUri()).isEqualTo("/videos/vi3N6cDinStg3oBbN79GklWS/captions/en");
            assertThat(res.getSrc()).isEqualTo("https://cdn.api.video/vod/vi3N6cDinStg3oBbN79GklWS/captions/en.vtt");
            assertThat(res.getSrclang()).isEqualTo("en");
            assertThat(res.getLanguageName()).isEqualTo("English");
            assertThat(res.getDefault()).isEqualTo(false);
        }

        @Test
        @DisplayName("400 response")
        public void responseWithStatus400Test() throws ApiException {
            answerOnAnyRequest(400, "");

            assertThatThrownBy(() -> api.upload("vi4k0jvEUuaTdRAEjQ4Prklg", "en", new File("")))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(400)).hasMessage("");
        }

        @Test
        @DisplayName("404 response")
        public void responseWithStatus404Test() throws ApiException {
            answerOnAnyRequest(404, "");

            assertThatThrownBy(() -> api.upload("vi4k0jvEUuaTdRAEjQ4Prklg", "en", new File("")))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(404)).hasMessage("");
        }
    }
}
