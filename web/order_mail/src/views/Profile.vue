<template>
  <div class="profile">
    <h2>个人中心</h2>

    <!-- 修改昵称 -->
    <section class="card section">
      <h3>修改昵称</h3>
      <form class="form-inline" @submit.prevent="saveNickname">
        <input v-model="nicknameForm.nickname" class="input" type="text" placeholder="昵称" />
        <button type="submit" class="btn btn-primary" :disabled="nicknameLoading">
          {{ nicknameLoading ? '保存中…' : '保存' }}
        </button>
      </form>
      <p v-if="nicknameMsg" :class="nicknameError ? 'error-msg' : 'success-msg'">{{ nicknameMsg }}</p>
    </section>

    <!-- 修改密码 -->
    <section class="card section">
      <h3>修改密码</h3>
      <form class="form-stack" @submit.prevent="savePassword">
        <div class="field">
          <label class="label">当前密码</label>
          <input v-model="passwordForm.oldPassword" class="input" type="password" required placeholder="当前密码" />
        </div>
        <div class="field">
          <label class="label">新密码</label>
          <input v-model="passwordForm.newPassword" class="input" type="password" required minlength="6" placeholder="6-20 位" />
        </div>
        <div class="field">
          <label class="label">确认新密码</label>
          <input v-model="passwordForm.confirmPassword" class="input" type="password" required placeholder="再次输入新密码" />
        </div>
        <p v-if="passwordMsg" :class="passwordError ? 'error-msg' : 'success-msg'">{{ passwordMsg }}</p>
        <button type="submit" class="btn btn-primary" :disabled="passwordLoading">
          {{ passwordLoading ? '提交中…' : '修改密码' }}
        </button>
      </form>
    </section>

    <!-- 收货地址 -->
    <section class="card section">
      <h3>收货地址</h3>
      <div class="address-list" v-if="addresses.length">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-item"
          :class="{ 'is-default': addr.isDefault }"
        >
          <div class="address-main">
            <span class="contact">{{ addr.contactName }} {{ addr.contactPhone }}</span>
            <span class="addr-text">{{ addr.fullAddress || [addr.province, addr.city, addr.district, addr.detail].filter(Boolean).join(' ') }}</span>
            <span v-if="addr.isDefault" class="default-tag">默认</span>
          </div>
          <div class="address-actions">
            <button type="button" class="btn btn-secondary btn-sm" @click="editAddress(addr)">编辑</button>
            <button v-if="!addr.isDefault" type="button" class="btn btn-secondary btn-sm" @click="setDefault(addr.id)">设为默认</button>
            <button type="button" class="btn btn-secondary btn-sm danger" @click="removeAddress(addr.id)">删除</button>
          </div>
        </div>
      </div>
      <p v-else class="empty-tip">暂无收货地址，请添加</p>

      <div class="address-form card sub-card">
        <h4>{{ editingAddress ? '编辑地址' : '新增地址' }}</h4>
        <form @submit.prevent="submitAddress">
          <div class="field">
            <label class="label">收货人</label>
            <input v-model="addressForm.contactName" class="input" required placeholder="姓名" />
          </div>
          <div class="field">
            <label class="label">手机号</label>
            <input v-model="addressForm.contactPhone" class="input" type="tel" required placeholder="11 位手机号" />
          </div>
          <div class="field">
            <label class="label">省/市/区</label>
            <div class="row-3">
              <input v-model="addressForm.province" class="input" placeholder="省" required />
              <input v-model="addressForm.city" class="input" placeholder="市" required />
              <input v-model="addressForm.district" class="input" placeholder="区/县" required />
            </div>
          </div>
          <div class="field">
            <label class="label">详细地址</label>
            <input v-model="addressForm.detail" class="input" required placeholder="街道、门牌号等" />
          </div>
          <div class="field checkbox-field">
            <label class="label-inline">
              <input v-model="addressForm.isDefault" type="checkbox" />
              设为默认地址
            </label>
          </div>
          <p v-if="addressMsg" :class="addressError ? 'error-msg' : 'success-msg'">{{ addressMsg }}</p>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="addressLoading">
              {{ addressLoading ? '提交中…' : (editingAddress ? '保存' : '添加') }}
            </button>
            <button v-if="editingAddress" type="button" class="btn btn-secondary" @click="cancelEdit">取消</button>
          </div>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuth } from '../composables/useAuth'
import { getProfile, updateProfile, changePassword } from '../api/user'
import {
  getAddressList,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
} from '../api/address'

const { user, setUser, loadUser } = useAuth()

// 昵称
const nicknameForm = reactive({ nickname: '' })
const nicknameLoading = ref(false)
const nicknameMsg = ref('')
const nicknameError = ref(false)

// 密码
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordLoading = ref(false)
const passwordMsg = ref('')
const passwordError = ref(false)

// 地址
const addresses = ref([])
const addressForm = reactive({
  id: null,
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false,
})
const editingAddress = ref(null)
const addressLoading = ref(false)
const addressMsg = ref('')
const addressError = ref(false)

function resetAddressForm() {
  addressForm.id = null
  addressForm.contactName = ''
  addressForm.contactPhone = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.detail = ''
  addressForm.isDefault = false
  editingAddress.value = null
  addressMsg.value = ''
}

async function loadAddresses() {
  try {
    const res = await getAddressList()
    addresses.value = res.data || []
  } catch (_) {
    addresses.value = []
  }
}

onMounted(async () => {
  try {
    const res = await getProfile()
    const info = res.data
    if (info) {
      nicknameForm.nickname = info.nickname || info.username || ''
    }
  } catch (_) {}
  loadAddresses()
})

async function saveNickname() {
  nicknameMsg.value = ''
  nicknameError.value = false
  nicknameLoading.value = true
  try {
    await updateProfile({ nickname: nicknameForm.nickname })
    nicknameMsg.value = '昵称已更新'
    setUser({ ...user.value, nickname: nicknameForm.nickname })
    loadUser()
  } catch (e) {
    nicknameError.value = true
    nicknameMsg.value = e.message || '保存失败'
  } finally {
    nicknameLoading.value = false
  }
}

async function savePassword() {
  passwordMsg.value = ''
  passwordError.value = false
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = true
    passwordMsg.value = '两次输入的新密码不一致'
    return
  }
  if (passwordForm.newPassword.length < 6) {
    passwordError.value = true
    passwordMsg.value = '新密码至少 6 位'
    return
  }
  passwordLoading.value = true
  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    passwordMsg.value = '密码已修改，请重新登录'
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    passwordError.value = true
    passwordMsg.value = e.message || '修改失败'
  } finally {
    passwordLoading.value = false
  }
}

function editAddress(addr) {
  editingAddress.value = addr
  addressForm.id = addr.id
  addressForm.contactName = addr.contactName || ''
  addressForm.contactPhone = addr.contactPhone || ''
  addressForm.province = addr.province || ''
  addressForm.city = addr.city || ''
  addressForm.district = addr.district || ''
  addressForm.detail = addr.detail || ''
  addressForm.isDefault = !!addr.isDefault
  addressMsg.value = ''
}

function cancelEdit() {
  resetAddressForm()
}

async function setDefault(id) {
  try {
    await setDefaultAddress(id)
    await loadAddresses()
  } catch (e) {
    alert(e.message || '设置失败')
  }
}

async function removeAddress(id) {
  if (!confirm('确定删除该地址？')) return
  try {
    await deleteAddress(id)
    if (editingAddress.value?.id === id) resetAddressForm()
    await loadAddresses()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

async function submitAddress() {
  if (!/^1[3-9]\d{9}$/.test(addressForm.contactPhone)) {
    addressError.value = true
    addressMsg.value = '手机号格式不正确'
    return
  }
  addressMsg.value = ''
  addressError.value = false
  addressLoading.value = true
  try {
    if (editingAddress.value) {
      await updateAddress({
        id: addressForm.id,
        contactName: addressForm.contactName,
        contactPhone: addressForm.contactPhone,
        province: addressForm.province,
        city: addressForm.city,
        district: addressForm.district,
        detail: addressForm.detail,
        isDefault: addressForm.isDefault,
      })
      addressMsg.value = '地址已更新'
    } else {
      await createAddress({
        contactName: addressForm.contactName,
        contactPhone: addressForm.contactPhone,
        province: addressForm.province,
        city: addressForm.city,
        district: addressForm.district,
        detail: addressForm.detail,
        isDefault: addressForm.isDefault,
      })
      addressMsg.value = '地址已添加'
    }
    resetAddressForm()
    await loadAddresses()
  } catch (e) {
    addressError.value = true
    addressMsg.value = e.message || '操作失败'
  } finally {
    addressLoading.value = false
  }
}
</script>

<style scoped>
.profile h2 {
  margin-bottom: 1.5rem;
}

.section {
  margin-bottom: 1.5rem;
}

.section h3 {
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.section h4 {
  margin-bottom: 0.75rem;
  font-size: 1rem;
}

.form-inline {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.form-inline .input {
  max-width: 240px;
}

.form-stack .field {
  margin-bottom: 1rem;
}

.form-stack .btn {
  margin-top: 0.25rem;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0.75rem;
  background: var(--bg);
  border-radius: var(--radius);
  border: 1px solid var(--border);
}

.address-item.is-default {
  border-color: var(--accent);
}

.address-main {
  flex: 1;
}

.contact {
  display: block;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.addr-text {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.default-tag {
  display: inline-block;
  margin-left: 0.5rem;
  padding: 0.1rem 0.4rem;
  font-size: 0.75rem;
  background: var(--accent);
  color: #fff;
  border-radius: 4px;
}

.address-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.btn-sm {
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
}

.btn-sm.danger:hover {
  background: rgba(248, 81, 73, 0.2);
  color: var(--danger);
}

.empty-tip {
  color: var(--text-muted);
  margin-bottom: 1rem;
}

.sub-card {
  background: var(--bg);
  padding: 1rem;
}

.row-3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0.5rem;
}

.checkbox-field .label-inline {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.9rem;
  color: var(--text-muted);
}

.form-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
</style>
