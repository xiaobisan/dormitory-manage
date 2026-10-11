import { defineStore } from 'pinia'
import { login as apiLogin, getMe } from '@/api/auth'
import { TOKEN_KEY } from '@/api/request'

const USER_KEY = 'dormity_user'

/**
 * 用户 store：token + 当前用户信息。
 * token 与 user 均持久化到 localStorage，刷新后自动恢复会话。
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.user?.role || '',
    username: (state) => state.user?.name || state.user?.username || ''
  },
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },
    setUser(user) {
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    // 登录：拿 token + 用户信息
    async login(payload) {
      const data = await apiLogin(payload)
      // 后端可能返回 {token, user} 或直接 token
      const token = data.token || data
      this.setToken(token)
      if (data.user) this.setUser(data.user)
      else await this.refreshMe()
      return data
    },
    async refreshMe() {
      try {
        const me = await getMe()
        if (me) this.setUser(me)
      } catch (e) {
        /* 401 已由拦截器处理 */
      }
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
    // 当前角色是否允许访问（roles 为空表示所有登录用户可访问）
    canAccess(roles) {
      if (!roles || roles.length === 0) return this.isLoggedIn
      return this.isLoggedIn && roles.includes(this.role)
    }
  }
})
