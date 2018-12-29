/* Copyright 2014 yiyuanzhong@gmail.com (Yiyuan Zhong)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __FLINTER_CRAWLER_H__
#define __FLINTER_CRAWLER_H__

#include <deque>
#include <map>
#include <sstream>
#include <string>

namespace CarreraProducer {

class HttpClient {
public:
    /// @param url actual url HttpClient going to crawl.
    /// @param hostname DNS is not working or you're doing something tricky.
    explicit HttpClient(const std::string &url, long connecttimeout = 5000L,
                    long timeout = 300000L, const std::string &hostname = std::string());

    ~HttpClient();

    // Only verify peer if set.
    bool SetCertificateAuthorityFile(const std::string &filename);

    bool Get(std::string *result);
    bool Post(std::string *result, const std::map<std::string, std::string> &http_headers = std::map<std::string, std::string> ());
    /*bool PostRaw(const std::string &content_type,
                 const std::string &raw,
                 std::string *result);*/

    // Remove all POST fields.
    void Clear();

    template <class T>
    void Set(const std::string &key, const T &value);
    void Set(const std::string &key, const char *value);
    void Set(const std::string &key, const std::string &value);

    const std::string &effective_url() const
    {
        return _effective_url;
    }

    const std::string &content_type() const
    {
        return _content_type;
    }

    long status() const
    {
        return _status;
    }

private:
    class Context;

    static size_t WriteFunction(char *ptr, size_t size, size_t nmemb, void *userdata);

    bool Initialize(const std::map<std::string, std::string>& http_headers = std::map<std::string, std::string> ());
    bool Request(std::string *result);
    bool SetMethod(bool get_or_post);

    std::map<std::string, std::string> _posts;
    std::deque<char> _result;
    long _connect_timeout_ms;
    long _timeout_ms;
    std::string _hostname;
    std::string _cainfo;
    std::string _url;

    std::string _effective_url;
    std::string _content_type;
    long _status;

    Context *_context;

}; // class HttpClient

template <class T>
void HttpClient::Set(const std::string &key, const T &value)
{
    std::ostringstream s;
    s << value;
    Set(key, s.str());
}

} // namespace CarreraProducer

#endif // __FLINTER_CRAWLER_H__
