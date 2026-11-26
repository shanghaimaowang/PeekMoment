// 联调入口。由调用方传入 M1 确认的服务地址、路径与会话令牌。
export function createRequest(baseUrl, readToken = () => '') {
  const base = baseUrl.replace(/\/$/, '')
  return ({path, method = 'GET', data}) => new Promise((resolve, reject) => {
    const token = readToken()
    uni.request({url: base + '/' + path.replace(/^\//, ''), method, data, timeout: 10000,
      header: token ? {Authorization: 'Bearer ' + token} : {},
      success: res => res.statusCode >= 200 && res.statusCode < 300
        ? resolve(res.data) : reject(new Error('请求失败（' + res.statusCode + '）')),
      fail: () => reject(new Error('网络连接失败，请稍后重试'))
    })
  })
}
