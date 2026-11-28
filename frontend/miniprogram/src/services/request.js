// readToken runs for each request so callers can clear or replace an in-memory token.
export function createRequest(baseUrl, readToken = () => '') {
  const base = baseUrl.replace(/\/$/, '')
  return ({path, method = 'GET', data}) => new Promise((resolve, reject) => {
    const token = readToken()
    uni.request({url: base + '/' + path.replace(/^\//, ''), method, data, timeout: 10000,
      header: token ? {Authorization: 'Bearer ' + token} : {},
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300) return resolve(res.data)
        const error = new Error(res.data?.message || '请求失败（' + res.statusCode + '）')
        error.statusCode = res.statusCode
        reject(error)
      },
      fail: () => reject(new Error('网络连接失败，请稍后重试'))
    })
  })
}
