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
    // Map backend avatar_path to avatarUrl for frontend compatibility
    if (newUser?.avatar_path) {
      newUser.avatarUrl = newUser.avatar_path
    }

    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  const setAvatarUrl = (url: string) => {
    if (!user.value?.username) return
    
    const updatedUser = { 
      ...(user.value || {}), 
      avatarUrl: url,
      avatar_path: url 
    }
    
    user.value = updatedUser
    localStorage.setItem('user', JSON.stringify(updatedUser))
    
    // Also update the legacy local storage key if needed, or just remove it.
    // For now, we'll keep it in sync but setUser won't prioritize it.
    if (url) {
      localStorage.setItem(`avatar:${user.value.username}`, url)
    } else {
      localStorage.removeItem(`avatar:${user.value.username}`)
    }
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, setToken, setUser, setAvatarUrl, logout }
})
