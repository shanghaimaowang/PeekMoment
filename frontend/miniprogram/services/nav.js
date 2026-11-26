export const open = (page, id) => uni.navigateTo({url: '/pages/' + page + '/index' + (id ? '?id=' + encodeURIComponent(id) : '')})
export const home = page => uni.reLaunch({url: '/pages/' + page + '/index'})
export const back = () => getCurrentPages().length > 1 ? uni.navigateBack() : home('feed')
export const toast = msg => uni.showToast({title: msg, icon:'none'})
export const problem = err => toast(err?.message || err?.errMsg || '操作失败，请重试')
export const editor = signedIn => signedIn ? open('compose') : uni.navigateTo({url:'/pages/login/index?next=compose'})
export function confirmAction(title, content) {
  return new Promise(resolve => uni.showModal({title, content, confirmText:'确定', cancelText:'取消',
    success: r => resolve(r.confirm), fail: () => resolve(false)}))
}
