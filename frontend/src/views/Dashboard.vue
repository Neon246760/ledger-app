<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6 tracking-tight">Dashboard</h1>

    <!-- Date Range Filter -->
    <div class="bg-white p-4 rounded-xl border border-gray-200 shadow-sm mb-6 flex flex-wrap items-center gap-4">
      <div class="flex items-center gap-2">
        <span class="text-gray-600 text-sm font-medium">Date Range:</span>
        <input
          type="date"
          v-model="startDate"
          class="bg-gray-50 border border-gray-300 rounded-lg p-2 text-gray-900 text-sm focus:ring-2 focus:ring-primary focus:border-primary outline-none"
        />
        <span class="text-gray-400">—</span>
        <input
          type="date"
          v-model="endDate"
          class="bg-gray-50 border border-gray-300 rounded-lg p-2 text-gray-900 text-sm focus:ring-2 focus:ring-primary focus:border-primary outline-none"
        />
      </div>
      <div class="text-xs text-gray-500">
        Default: current month
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <!-- Summary Cards -->
      <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow">
        <h3 class="text-gray-500 text-sm font-medium uppercase tracking-wider mb-2">Total Income</h3>
        <div class="text-3xl font-bold text-green-600">¥ {{ summary.total_income.toFixed(2) }}</div>
      </div>
      <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow">
        <h3 class="text-gray-500 text-sm font-medium uppercase tracking-wider mb-2">Total Expense</h3>
        <div class="text-3xl font-bold text-red-600">¥ {{ summary.total_expense.toFixed(2) }}</div>
      </div>
      <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-shadow">
        <h3 class="text-gray-500 text-sm font-medium uppercase tracking-wider mb-2">Balance</h3>
        <div class="text-3xl font-bold" :class="summary.balance < 0 ? 'text-red-600' : (summary.balance > 0 ? 'text-green-600' : 'text-gray-900')">¥ {{ summary.balance.toFixed(2) }}</div>
      </div>
    </div>
    
    <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
      <h2 class="text-xl font-bold text-gray-900 mb-6 tracking-tight">Category Management</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="md:col-span-1">
          <div class="space-y-3">
            <label class="block text-sm font-medium text-gray-700">Category Type</label>
            <div class="flex gap-2">
              <button
                :class="newCategoryType === 'expense' ? 'bg-purple-50 text-primary border-purple-200' : 'bg-gray-50 text-gray-600 border-gray-200'"
                class="px-3 py-2 rounded-lg border font-medium"
                @click="newCategoryType = 'expense'"
              >
                Expense
              </button>
              <button
                :class="newCategoryType === 'income' ? 'bg-purple-50 text-primary border-purple-200' : 'bg-gray-50 text-gray-600 border-gray-200'"
                class="px-3 py-2 rounded-lg border font-medium"
                @click="newCategoryType = 'income'"
              >
                Income
              </button>
            </div>
            <label class="block text-sm font-medium text-gray-700">Category Name</label>
            <input
              v-model="newCategoryName"
              type="text"
              class="w-full bg-gray-50 border border-gray-300 rounded-lg p-2.5 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all"
              placeholder="Enter category name"
            />
            <button
              class="w-full bg-primary hover:bg-purple-600 text-white font-bold py-2.5 px-5 rounded-lg transition-all shadow-sm hover:shadow-md flex items-center justify-center gap-2"
              @click="addCategory"
            >
              <PlusIcon class="w-5 h-5" />
              Add Category
            </button>
          </div>
        </div>
        <div class="md:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="border border-gray-200 rounded-xl p-4">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-sm font-semibold text-gray-700">Income Categories</h3>
              <span class="text-xs text-gray-500">{{ categoryStore.incomeCategories.length }}</span>
            </div>
            <div class="flex flex-wrap gap-2" @dragover.prevent>
              <span
                v-for="(cat, idx) in categoryStore.incomeCategories"
                :key="cat"
                class="inline-flex items-center gap-2 bg-green-50 text-green-700 border border-green-200 px-3 py-1.5 rounded-full text-sm cursor-move transition-all"
                draggable="true"
                @dragstart="onDragStart('income', idx)"
                @dragover.prevent
                @dragenter="onDragEnter('income', idx)"
                @drop="onDrop('income', idx)"
              >
                {{ cat }}
                <button class="text-green-700/70 hover:text-green-800" @click="removeIncome(cat)">
                  <TrashIcon class="w-4 h-4" />
                </button>
              </span>
              <span
                v-for="(cat, idx) in categoryStore.incomeCategories"
                :key="'indicator-income-'+idx"
                v-if="dragType === 'income' && dragOverIndex === idx"
                class="w-2 h-7 bg-primary/20 rounded transition-all"
              />
            </div>
          </div>
          <div class="border border-gray-200 rounded-xl p-4">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-sm font-semibold text-gray-700">Expense Categories</h3>
              <span class="text-xs text-gray-500">{{ categoryStore.expenseCategories.length }}</span>
            </div>
            <div class="flex flex-wrap gap-2" @dragover.prevent>
              <span
                v-for="(cat, idx) in categoryStore.expenseCategories"
                :key="cat"
                class="inline-flex items-center gap-2 bg-red-50 text-red-700 border border-red-200 px-3 py-1.5 rounded-full text-sm cursor-move transition-all"
                draggable="true"
                @dragstart="onDragStart('expense', idx)"
                @dragover.prevent
                @dragenter="onDragEnter('expense', idx)"
                @drop="onDrop('expense', idx)"
              >
                {{ cat }}
                <button class="text-red-700/70 hover:text-red-800" @click="removeExpense(cat)">
                  <TrashIcon class="w-4 h-4" />
                </button>
              </span>
              <span
                v-for="(cat, idx) in categoryStore.expenseCategories"
                :key="'indicator-expense-'+idx"
                v-if="dragType === 'expense' && dragOverIndex === idx"
                class="w-2 h-7 bg-primary/20 rounded transition-all"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import api from '@/api'
import dayjs from 'dayjs'
import { useCategoryStore } from '@/stores/category'
import { Trash2 as TrashIcon, Plus as PlusIcon } from 'lucide-vue-next'

const summary = ref({
  total_income: 0,
  total_expense: 0,
  balance: 0
})
const loading = ref(true)

const startDate = ref(dayjs().startOf('month').format('YYYY-MM-DD'))
const endDate = ref(dayjs().endOf('month').format('YYYY-MM-DD'))

const fetchSummary = async () => {
  loading.value = true
  try {
    const start = dayjs(startDate.value).startOf('day').format('YYYY-MM-DDTHH:mm:ss')
    const end = dayjs(endDate.value).endOf('day').format('YYYY-MM-DDTHH:mm:ss')
    const res = await api.get('/transactions/summary/statistics', {
      params: { start_date: start, end_date: end }
    })
    summary.value = res.data
  } catch (e) {
    console.error('Failed to fetch summary', e)
  } finally {
    loading.value = false
  }
}

watch([startDate, endDate], fetchSummary)
onMounted(fetchSummary)

const categoryStore = useCategoryStore()
const newCategoryName = ref('')
const newCategoryType = ref<'income' | 'expense'>('expense')

onMounted(() => {
  categoryStore.reloadFromStorage()
})

const addCategory = () => {
  categoryStore.addCategory(newCategoryType.value, newCategoryName.value)
  newCategoryName.value = ''
}

const removeIncome = (name: string) => categoryStore.removeCategory('income', name)
const removeExpense = (name: string) => categoryStore.removeCategory('expense', name)

const dragType = ref<'income' | 'expense' | null>(null)
const dragIndex = ref<number | null>(null)
const dragOverIndex = ref<number | null>(null)
const onDragStart = (type: 'income' | 'expense', index: number) => {
  dragType.value = type
  dragIndex.value = index
}
const onDragEnter = (type: 'income' | 'expense', index: number) => {
  if (dragType.value !== type) return
  dragOverIndex.value = index
}
const onDrop = (type: 'income' | 'expense', index: number) => {
  if (dragType.value !== type || dragIndex.value === null) return
  categoryStore.reorderCategory(type, dragIndex.value, index)
  dragType.value = null
  dragIndex.value = null
  dragOverIndex.value = null
}
</script>
