<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Transactions</h1>
      <button @click="showAddModal = true" class="bg-primary hover:bg-purple-600 text-white font-bold py-2.5 px-5 rounded-lg transition-all shadow-sm hover:shadow-md flex items-center gap-2">
        <PlusIcon class="w-5 h-5" />
        Add New
      </button>
    </div>

    <!-- Filters -->
    <div class="bg-white p-4 rounded-xl border border-gray-200 mb-6 flex flex-wrap gap-4 shadow-sm">
      <div class="flex items-center gap-2 flex-wrap">
        <span class="text-gray-600 text-sm font-medium">Type:</span>
        <select v-model="filters.type" class="bg-gray-50 border border-gray-300 rounded-lg p-2 text-gray-900 text-sm focus:ring-2 focus:ring-primary focus:border-primary outline-none">
          <option value="">All</option>
          <option value="income">Income</option>
          <option value="expense">Expense</option>
        </select>
      </div>
      <div class="flex items-center gap-2 flex-wrap">
        <span class="text-gray-600 text-sm font-medium">Date Range:</span>
        <input
          type="date"
          v-model="filters.startDate"
          class="bg-gray-50 border border-gray-300 rounded-lg p-2 text-gray-900 text-sm focus:ring-2 focus:ring-primary focus:border-primary outline-none w-36 sm:w-auto"
        />
        <span class="text-gray-400">—</span>
        <input
          type="date"
          v-model="filters.endDate"
          class="bg-gray-50 border border-gray-300 rounded-lg p-2 text-gray-900 text-sm focus:ring-2 focus:ring-primary focus:border-primary outline-none w-36 sm:w-auto"
        />
      </div>
    </div>

    <!-- List -->
    <div class="bg-white rounded-xl border border-gray-200 shadow-sm">
      <div class="overflow-x-auto">
        <table class="min-w-[720px] w-full text-left border-collapse">
        <thead class="bg-gray-50 text-gray-500 text-xs uppercase tracking-wider font-semibold">
          <tr>
            <th class="p-4 border-b border-gray-200">Date</th>
            <th class="p-4 border-b border-gray-200">Category</th>
            <th class="p-4 border-b border-gray-200">Description</th>
            <th class="p-4 border-b border-gray-200 text-right">Amount</th>
            <th class="p-4 border-b border-gray-200 text-center">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-if="loading" class="text-center">
            <td colspan="5" class="p-8 text-gray-500">Loading...</td>
          </tr>
          <tr v-else-if="transactions.length === 0" class="text-center">
            <td colspan="5" class="p-8 text-gray-500 bg-gray-50/50">No transactions found</td>
          </tr>
          <tr v-for="t in transactions" :key="t.id" class="hover:bg-gray-50 transition-colors">
            <td class="p-4 text-gray-900 font-medium">{{ formatDate(t.date) }}</td>
            <td class="p-4 text-gray-700">
              <span class="px-2.5 py-1 rounded-full text-xs font-medium" :class="t.type === 'income' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
                {{ t.category }}
              </span>
            </td>
            <td class="p-4 text-gray-600">{{ t.description }}</td>
            <td class="p-4 text-right font-semibold" :class="t.type === 'income' ? 'text-green-600' : 'text-red-600'">
              {{ t.type === 'income' ? '+' : '-' }} {{ t.amount.toFixed(2) }}
            </td>
            <td class="p-4 text-center">
              <button @click="deleteTransaction(t.id)" class="text-gray-400 hover:text-red-500 transition-colors p-1 rounded hover:bg-red-50">
                <TrashIcon class="w-4 h-4" />
              </button>
            </td>
          </tr>
        </tbody>
        </table>
      </div>
    </div>

    <!-- Add Modal -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div class="bg-white w-full max-w-md rounded-xl border border-gray-200 shadow-2xl p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-6">Add Transaction</h2>
        <form @submit.prevent="addTransaction" class="space-y-4">
          <div class="flex gap-4 p-1 bg-gray-100 rounded-lg">
            <label class="flex-1 cursor-pointer">
              <input type="radio" v-model="form.type" value="expense" class="hidden peer" />
              <div class="text-center p-2 rounded-md text-gray-500 font-medium transition-all peer-checked:bg-white peer-checked:text-red-600 peer-checked:shadow-sm">Expense</div>
            </label>
            <label class="flex-1 cursor-pointer">
              <input type="radio" v-model="form.type" value="income" class="hidden peer" />
              <div class="text-center p-2 rounded-md text-gray-500 font-medium transition-all peer-checked:bg-white peer-checked:text-green-600 peer-checked:shadow-sm">Income</div>
            </label>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Amount</label>
            <input v-model.number="form.amount" type="number" step="0.01" required class="w-full bg-gray-50 border border-gray-300 rounded-lg p-2.5 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Category</label>
            <select v-model="form.category" required class="w-full bg-gray-50 border border-gray-300 rounded-lg p-2.5 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all">
              <option v-for="cat in (form.type === 'income' ? categoryStore.incomeCategories : categoryStore.expenseCategories)" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Date</label>
            <input v-model="form.date" type="datetime-local" required class="w-full bg-gray-50 border border-gray-300 rounded-lg p-2.5 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <input v-model="form.description" type="text" class="w-full bg-gray-50 border border-gray-300 rounded-lg p-2.5 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all" />
          </div>
          <div class="flex justify-end gap-3 mt-8 pt-4 border-t border-gray-100">
            <button type="button" @click="showAddModal = false" class="px-4 py-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors font-medium">Cancel</button>
            <button type="submit" class="px-4 py-2 bg-primary hover:bg-purple-600 text-white rounded-lg font-bold transition-colors shadow-sm hover:shadow">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import api from '@/api'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import { useCategoryStore } from '@/stores/category'
import { Plus as PlusIcon, Trash2 as TrashIcon } from 'lucide-vue-next'

dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.tz.setDefault('Asia/Shanghai')

const transactions = ref<any[]>([])
const loading = ref(false)
const showAddModal = ref(false)
const filters = ref({
  type: '',
  startDate: dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
  endDate: dayjs().format('YYYY-MM-DD')
})

const form = ref({
  type: 'expense',
  amount: 0,
  category: '',
  date: dayjs().format('YYYY-MM-DDTHH:mm'),
  description: ''
})

const categoryStore = useCategoryStore()
onMounted(() => {
  categoryStore.reloadFromStorage()
})

const fetchTransactions = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (filters.value.type) params.type = filters.value.type
    if (filters.value.startDate) {
      params.start_date = dayjs(filters.value.startDate).startOf('day').format('YYYY-MM-DDTHH:mm:ss')
    }
    if (filters.value.endDate) {
      params.end_date = dayjs(filters.value.endDate).endOf('day').format('YYYY-MM-DDTHH:mm:ss')
    }
    const res = await api.get('/transactions', { params })
    transactions.value = res.data.items
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const addTransaction = async () => {
  try {
    await api.post('/transactions', {
      ...form.value,
      date: dayjs.tz(form.value.date, 'Asia/Shanghai').toISOString()
    })
    showAddModal.value = false
    fetchTransactions()
    // Reset form
    form.value = {
      type: 'expense',
      amount: 0,
      category: '',
      date: dayjs().format('YYYY-MM-DDTHH:mm'),
      description: ''
    }
  } catch (e) {
    console.error(e)
    alert('Failed to add transaction')
  }
}

const deleteTransaction = async (id: number) => {
  if (!confirm('Are you sure?')) return
  try {
    await api.delete(`/transactions/${id}`)
    fetchTransactions()
  } catch (e) {
    console.error(e)
  }
}

const formatDate = (date: string) => {
  const hasTZ = /Z$|[\+\-]\d{2}:\d{2}$/.test(date)
  const base = hasTZ ? dayjs(date) : dayjs.utc(date)
  return base.tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm')
}

watch(filters, fetchTransactions, { deep: true })

onMounted(fetchTransactions)
</script>
