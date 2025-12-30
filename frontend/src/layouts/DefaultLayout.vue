<template>
  <div class="min-h-screen bg-gray-50 flex text-gray-900 font-sans">
    <!-- Sidebar -->
    <aside class="w-64 bg-white border-r border-gray-200 hidden md:flex flex-col shadow-sm z-10">
      <div class="p-6 text-2xl font-bold text-primary tracking-tight flex items-center gap-2">
        <span class="bg-primary text-white p-1 rounded">L</span>EDGER
      </div>
      <nav class="flex-1 px-4 py-4 space-y-1">
        <router-link to="/" class="nav-item" exact-active-class="router-link-active">
          Dashboard
        </router-link>
        <router-link to="/transactions" class="nav-item">
          Transactions
        </router-link>
        <router-link to="/statistics" class="nav-item">
          Statistics
        </router-link>
        <router-link to="/budget" class="nav-item">
          Budget
        </router-link>
      </nav>
      <div class="p-4 border-t border-gray-100">
        <div
          class="flex items-center gap-3 mb-4 p-2 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
          @click="toggleUserPanel"
        >
          <div class="relative w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold shadow-sm overflow-hidden group">
            <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" class="w-full h-full object-cover" />
            <span v-else>{{ userInitial }}</span>
            <div
              class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center"
              title="Edit avatar"
            >
              <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1.003 1.003 0 0 0 0-1.42l-2.34-2.34a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.84-1.82z"/></svg>
            </div>
          </div>
          <div class="overflow-hidden">
            <div class="text-sm font-medium text-gray-900 truncate">{{ authStore.user?.username || 'User' }}</div>
          </div>
        </div>
        <button @click="logout" class="w-full py-2.5 px-4 bg-white hover:bg-red-50 text-gray-600 hover:text-red-600 rounded-lg transition-all text-sm border border-gray-200 hover:border-red-200 shadow-sm">
          Logout
        </button>
      </div>
    </aside>

    <!-- Mobile Header -->
    <div class="flex-1 flex flex-col min-w-0 bg-gray-50">
      <header class="md:hidden bg-white border-b border-gray-200 p-4 flex justify-between items-center shadow-sm sticky top-0 z-20">
        <div class="text-xl font-bold text-primary">LEDGER</div>
        <button class="text-gray-600 p-2 border border-gray-200 rounded-lg hover:bg-gray-50" @click="toggleMobileNav">Menu</button>
      </header>

      <!-- Main Content -->
      <main class="flex-1 p-4 md:p-8 overflow-y-auto">
        <div class="max-w-7xl mx-auto">
          <router-view />
        </div>
      </main>
    </div>
  </div>

  <!-- User Panel Modal -->
  <div v-if="showUserPanel" class="fixed inset-0 bg-black/40 z-50 flex items-end md:items-center justify-center p-4" @click.self="showUserPanel=false">
    <div class="w-full max-w-md bg-white rounded-xl border border-gray-200 shadow-2xl p-6">
      <div class="flex items-center justify-between mb-2">
        <h3 class="text-lg font-bold text-gray-900">User Settings</h3>
        <button class="text-gray-500 hover:text-gray-900" @click="showUserPanel=false">✕</button>
      </div>
      <div class="border-b border-gray-200 mb-4"></div>

      <!-- Avatar Upload -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">Avatar</label>
        <div class="flex items-center gap-4">
          <div class="relative w-16 h-16 rounded-full bg-gray-100 overflow-hidden group">
            <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" class="w-full h-full object-cover" @error="onAvatarError" />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-500 font-bold">{{ userInitial }}</div>
            <button
              class="absolute inset-0 opacity-0 group-hover:opacity-100 bg-black/30 text-white flex items-center justify-center transition-opacity"
              title="Change avatar"
              @click="triggerAvatarInput"
            >
              <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1.003 1.003 0 0 0 0-1.42l-2.34-2.34a1.003 1.003 0 0 0-1.42 0l-1.83 1.83 3.75 3.75 1.84-1.82z"/></svg>
            </button>
          </div>
          <input ref="avatarInput" type="file" accept="image/*" class="hidden" @change="onAvatarSelected" />
        </div>
      </div>

      <!-- Account Setting Section -->
      <div class="mt-2 mb-3">
        <label class="block text-sm font-medium text-gray-700">Account Setting</label>
        <div class="border-b border-gray-200 mt-2"></div>
      </div>

      <!-- Change Password -->
      <div class="mb-4">
        <button class="px-3 py-2 bg-white text-gray-800 border border-gray-300 hover:bg-gray-100 rounded-md font-medium transition-colors" @click="togglePassword">
          Change Password
        </button>
        <div v-if="showPassword" class="mt-4 border border-gray-200 rounded-lg p-4 bg-gray-50">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">New Password</label>
              <input v-model="newPassword" type="password" class="w-full bg-gray-50 border border-gray-300 rounded-lg p-2.5 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Repeat Password</label>
              <input v-model="repeatPassword" type="password" class="w-full bg-gray-50 border border-gray-300 rounded-lg p-2.5 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none" />
            </div>
            <div class="md:col-span-2 flex justify-end gap-3">
              <button class="px-3 py-2 bg-white text-gray-700 border border-gray-300 hover:bg-gray-100 rounded-md" @click="togglePassword">Cancel</button>
              <button class="px-3 py-2 bg-primary hover:bg-purple-600 text-white rounded-md font-medium" @click="submitPassword">Save</button>
            </div>
            <div v-if="passwordError" class="md:col-span-2 text-red-600 text-sm">{{ passwordError }}</div>
          </div>
        </div>
      </div>

      <!-- Delete Account -->
      <div class="mb-2">
        <button class="px-3 py-2 bg-white text-red-700 border border-red-300 hover:bg-red-50 rounded-md font-medium transition-colors" @click="toggleDelete">
          Delete Account
        </button>
      </div>
      <div v-if="showDelete" class="mt-3 border border-red-200 rounded-lg p-4 bg-red-50">
        <div class="text-sm text-red-700 mb-3">This action is irreversible. Please confirm by entering your password.</div>
        <input v-model="confirmPassword" type="password" placeholder="Enter your password" class="w-full bg-white border border-red-300 rounded-lg p-2.5 text-red-900 focus:ring-2 focus:ring-red-400 focus:border-red-400 outline-none" />
        <div class="flex justify-end gap-3 mt-3">
          <button class="px-3 py-2 text-red-700 hover:text-red-900 rounded-lg" @click="toggleDelete">Cancel</button>
          <button class="px-3 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-bold" @click="submitDelete">Confirm Delete</button>
        </div>
        <div v-if="deleteError" class="text-red-700 text-sm mt-2">{{ deleteError }}</div>
      </div>
    </div>
  </div>

  <!-- Mobile Nav Drawer -->
  <div v-if="showMobileNav" class="fixed inset-0 z-50">
    <div class="absolute inset-0 bg-black/40" @click="toggleMobileNav"></div>
    <div class="absolute left-0 top-0 bottom-0 w-72 bg-white border-r border-gray-200 shadow-xl p-4">
      <div class="flex items-center justify-between mb-4">
        <div class="text-lg font-bold text-primary">Menu</div>
        <button class="text-gray-500 hover:text-gray-900" @click="toggleMobileNav">✕</button>
      </div>
      <div
        class="flex items-center gap-3 mb-4 p-2 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
        @click="openUserFromMobile"
      >
        <div class="relative w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold shadow-sm overflow-hidden">
          <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" class="w-full h-full object-cover" />
          <span v-else>{{ userInitial }}</span>
        </div>
        <div class="overflow-hidden">
          <div class="text-sm font-medium text-gray-900 truncate">{{ authStore.user?.username || 'User' }}</div>
          <div class="text-xs text-gray-500">User Settings</div>
        </div>
      </div>
      <nav class="space-y-2">
        <router-link to="/" class="nav-item" exact-active-class="router-link-active" @click="toggleMobileNav">Dashboard</router-link>
        <router-link to="/transactions" class="nav-item" @click="toggleMobileNav">Transactions</router-link>
        <router-link to="/statistics" class="nav-item" @click="toggleMobileNav">Statistics</router-link>
        <router-link to="/budget" class="nav-item" @click="toggleMobileNav">Budget</router-link>
      </nav>
      <div class="mt-6">
        <button @click="logout" class="w-full py-2.5 px-4 bg-white hover:bg-red-50 text-gray-600 hover:text-red-600 rounded-lg transition-all text-sm border border-gray-200 hover:border-red-200 shadow-sm">
          Logout
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import api from '@/api'

const authStore = useAuthStore()
const router = useRouter()

const userInitial = computed(() => {
  return authStore.user?.username?.charAt(0).toUpperCase() || 'U'
})

const logout = () => {
  authStore.logout()
  router.push('/login')
}

const avatarUrl = computed(() => authStore.user?.avatarUrl || '')
const showUserPanel = ref(false)
const showMobileNav = ref(false)
const showPassword = ref(false)
const showDelete = ref(false)
const newPassword = ref('')
const repeatPassword = ref('')
const confirmPassword = ref('')
const passwordError = ref('')
const deleteError = ref('')
const avatarInput = ref<HTMLInputElement | null>(null)

const toggleUserPanel = () => { showUserPanel.value = true }
const toggleMobileNav = () => { showMobileNav.value = !showMobileNav.value }
const openUserFromMobile = () => { showMobileNav.value = false; showUserPanel.value = true }
const togglePassword = () => { showPassword.value = !showPassword.value; passwordError.value = '' }
const toggleDelete = () => { showDelete.value = !showDelete.value; deleteError.value = '' }

const triggerAvatarInput = () => avatarInput.value?.click()
const onAvatarSelected = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await api.post('/upload/image', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    const url = res.data.url
    authStore.setAvatarUrl(url)
  } catch (err) {
    console.error(err)
  }
}

const onAvatarError = () => {
  authStore.setUser({ ...(authStore.user || {}), avatarUrl: '' })
}
const submitPassword = async () => {
  passwordError.value = ''
  if (!newPassword.value || newPassword.value.length < 6) {
    passwordError.value = 'Password must be at least 6 characters'
    return
  }
  if (newPassword.value !== repeatPassword.value) {
    passwordError.value = 'Passwords do not match'
    return
  }
  try {
    await api.post('/user/password', { password: newPassword.value, repeat_password: repeatPassword.value })
    showPassword.value = false
    newPassword.value = ''
    repeatPassword.value = ''
  } catch (err: any) {
    passwordError.value = err.response?.data?.detail || 'Failed to update password'
  }
}

const submitDelete = async () => {
  deleteError.value = ''
  if (!confirmPassword.value) {
    deleteError.value = 'Please enter your password'
    return
  }
  try {
    await api.post('/user/delete', { password: confirmPassword.value, avatar_url: avatarUrl.value })
    authStore.setAvatarUrl('')
    authStore.logout()
    router.push('/register')
  } catch (err: any) {
    deleteError.value = err.response?.data?.detail || 'Failed to delete account'
  }
}
</script>

<style scoped>
.nav-item {
  @apply flex items-center px-4 py-3 text-gray-600 rounded-lg hover:bg-gray-50 hover:text-primary transition-all font-medium border border-transparent;
}
.nav-item.router-link-active {
  @apply bg-purple-50 text-primary border-purple-100 shadow-sm;
}
</style>
