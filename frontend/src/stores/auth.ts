import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const getStoredAvatar = (username: string | undefined | null) => {
    if (!username) return ''
    return localStorage.getItem(`avatar:${username}`) || ''
  }

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUser = (newUser: any) => {
    const avatarUrl = getStoredAvatar(newUser?.username)
    user.value = avatarUrl ? { ...newUser, avatarUrl } : newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  const setAvatarUrl = (url: string) => {
    if (!user.value?.username) return
    if (url) {
      localStorage.setItem(`avatar:${user.value.username}`, url)
      user.value = { ...(user.value || {}), avatarUrl: url }
    } else {
      localStorage.removeItem(`avatar:${user.value.username}`)
      const updated = { ...(user.value || {}) }
      delete (updated as any).avatarUrl
      user.value = updated
    }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, setToken, setUser, setAvatarUrl, logout }
})
