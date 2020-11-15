import groovy.json.JsonSlurper

import java.time.Instant
import java.time.ZoneId

class ImageInfo {

    protected apiUrl, imageName, authToken

    ImageInfo(String apiUrl, imageName, authToken) {
        this.apiUrl = apiUrl
        this.imageName = imageName
        this.authToken = authToken
    }

    HashMap getLabeledTagList() {

        def response = this.performRequest(this.getUrl(this.imageName + '/tags/list'))

        def tagsList = [:]

        response.tags.each { tag ->
            tagsList[this.makeLabel(response.name, tag)] = response.name + ":" + tag
        }

        return tagsList
    }

    /**
     *
     * @param uri
     * @return
     */
    protected getUrl(uri) {
        return [this.apiUrl, uri].join('/')
    }

    /**
     *
     * @param tag
     * @return
     */
    protected makeLabel(name, tag) {
    //    def time = Instant.parse(this.getImageDate(tag)).atZone(ZoneId.of('UTC')).format('E MMM dd HH:mm:ss z yyyy')

        return name + ':' + tag
    }


    /**
     *
     * @param url
     * @return
     */
    protected Object performRequest(url) {

        def http_client = new URL(url).openConnection() as HttpURLConnection

        http_client.setRequestMethod('GET')

        http_client.setRequestProperty("Authorization", "Basic " + this.authToken)

        http_client.connect()

        if (http_client.responseCode == 200) {
            return new JsonSlurper().parseText(http_client.inputStream.getText('UTF-8'))
        } else {
            println("HTTP response error")
            System.exit(0)
        }
    }

    /**
     *
     * @param image
     * @param tag
     * @return
     */
    protected String getImageDate(String tag) {

        def url = this.getUrl(this.imageName + '/manifests/' + tag)

        def response = this.performRequest(url);

        def caps = new JsonSlurper().parseText(response.history[0].v1Compatibility)

        return caps.created;
    }
}
