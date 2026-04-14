<template>
  <div class="ops-page">
    <section class="hero">
      <div>
        <div class="hero-kicker">FAMILY BRIDGE</div>
        <h2>管理员先把家属资料建好，再把多个视障人士绑定进来。</h2>
        <p>这一页同时处理家属资料、绑定关系和解绑操作，让“一个家属对应多个视障人士、一个视障人士对应多个家属”的协同路径一次跑通。</p>
      </div>
      <div class="hero-actions">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索家属姓名 / 手机 / 用户名"
          allow-clear
          enter-button="搜索"
          @search="loadAll"
        />
        <a-button size="large" @click="resetSearch">重置</a-button>
        <a-button type="primary" size="large" @click="openFamilyModal()">新增家属</a-button>
      </div>
    </section>

    <section class="stats-grid">
      <div class="stat-card accent">
        <div class="stat-label">家属人数</div>
        <div class="stat-value">{{ familyMembers.length }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">绑定总数</div>
        <div class="stat-value">{{ bindings.length }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已覆盖视障人士</div>
        <div class="stat-value">{{ coveredBlindUserCount }}</div>
      </div>
    </section>

    <a-card class="surface-card" :bordered="false">
      <template #title>
        <div class="table-headline">
          <div>
            <div class="table-title">家属列表</div>
            <div class="table-subtitle">先维护家属，再从家属维度管理绑定关系</div>
          </div>
          <a-tag color="cyan">共 {{ familyMembers.length }} 位</a-tag>
        </div>
      </template>
      <a-table
        row-key="id"
        :loading="loading"
        :data-source="familyMembers"
        :columns="familyColumns"
        :pagination="{ pageSize: 8, showSizeChanger: false }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div>
              <div class="strong">{{ record.name }}</div>
              <div class="muted">{{ record.phone || '未填写手机号' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'email'">
            <span class="muted">{{ record.email || '未填写邮箱' }}</span>
          </template>
          <template v-else-if="column.key === 'bindingCount'">
            <a-tag color="geekblue">{{ record.bindingCount }} 人</a-tag>
          </template>
          <template v-else-if="column.key === 'updatedAt'">
            <span class="muted">{{ formatDateTime(record.updatedAt) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-row">
              <a-button type="link" @click="openFamilyDrawer(record)">查看绑定</a-button>
              <a-button type="link" @click="openFamilyModal(record)">编辑</a-button>
              <a-popconfirm
                title="确认删除该家属吗？删除后会清理绑定关系。"
                ok-text="删除"
                cancel-text="取消"
                @confirm="handleDeleteFamily(record)"
              >
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card class="surface-card" :bordered="false">
      <template #title>
        <div class="table-headline">
          <div>
            <div class="table-title">绑定总览</div>
            <div class="table-subtitle">快速查看当前家属与视障人士的全部协同关系</div>
          </div>
          <a-button type="primary" @click="openBindingModal()">新增绑定</a-button>
        </div>
      </template>
      <a-table
        row-key="id"
        :loading="loading"
        :data-source="bindings"
        :columns="bindingColumns"
        :pagination="{ pageSize: 8, showSizeChanger: false }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'familyName'">
            <div>
              <div class="strong">{{ record.familyName }}</div>
              <div class="muted">{{ record.familyPhone || '未填写手机号' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'user'">
            <div>
              <div class="strong">{{ record.nickname }}</div>
              <div class="muted">{{ record.username }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'orange'">{{ record.status }}</a-tag>
          </template>
          <template v-else-if="column.key === 'createdAt'">
            <span class="muted">{{ formatDateTime(record.createdAt) }}</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="familyModalOpen"
      :title="familyEditingId ? '编辑家属' : '新增家属'"
      :confirm-loading="submitLoading"
      @ok="handleFamilySubmit"
      @cancel="handleFamilyCancel"
    >
      <a-form ref="familyFormRef" :model="familyFormState" :label-col="{ span: 6 }" :wrapper-col="{ span: 17 }">
        <a-form-item label="家属姓名" name="name" :rules="[{ required: true, message: '请输入家属姓名' }]">
          <a-input v-model:value="familyFormState.name" />
        </a-form-item>
        <a-form-item label="手机号" name="phone" :rules="[{ pattern: PHONE_PATTERN, message: '手机号格式不正确' }]">
          <a-input v-model:value="familyFormState.phone" />
        </a-form-item>
        <a-form-item label="邮箱" name="email" :rules="[{ pattern: EMAIL_PATTERN, message: '邮箱格式不正确' }]">
          <a-input v-model:value="familyFormState.email" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="bindingModalOpen"
      title="新增绑定"
      :confirm-loading="bindingSubmitLoading"
      @ok="handleBindingSubmit"
      @cancel="handleBindingCancel"
    >
      <a-form ref="bindingFormRef" :model="bindingFormState" :label-col="{ span: 6 }" :wrapper-col="{ span: 17 }">
        <a-form-item label="家属" name="familyMemberId" :rules="[{ required: true, message: '请选择家属' }]">
          <a-select
            v-model:value="bindingFormState.familyMemberId"
            :options="familyMemberOptions"
            placeholder="选择家属"
            show-search
            :filter-option="filterOption"
          />
        </a-form-item>
        <a-form-item label="视障人士" name="userId" :rules="[{ required: true, message: '请选择视障人士' }]">
          <a-select
            v-model:value="bindingFormState.userId"
            :options="blindUserOptions"
            placeholder="选择视障人士"
            show-search
            :filter-option="filterOption"
          />
        </a-form-item>
        <a-form-item label="关系" name="relationship" :rules="[{ required: true, message: '请输入关系' }]">
          <a-input v-model:value="bindingFormState.relationship" placeholder="如：女儿、儿子、配偶、监护人" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer
      v-model:open="drawerOpen"
      :title="drawerFamily ? `${drawerFamily.name} 的绑定关系` : '家属绑定关系'"
      width="560"
      destroy-on-close
    >
      <template #extra>
        <a-button type="primary" @click="openBindingModal(drawerFamily?.id)">新增绑定</a-button>
      </template>

      <div v-if="drawerFamily" class="drawer-header">
        <div class="drawer-name">{{ drawerFamily.name }}</div>
        <div class="drawer-meta">{{ drawerFamily.phone || '未填写手机号' }} · {{ drawerFamily.email || '未填写邮箱' }}</div>
      </div>

      <a-list :data-source="drawerBindings" :loading="drawerLoading" item-layout="horizontal">
        <template #renderItem="{ item }">
          <a-list-item>
            <template #actions>
              <a-popconfirm
                title="确认解绑这位视障人士吗？"
                ok-text="解绑"
                cancel-text="取消"
                @confirm="handleUnbind(item)"
              >
                <a-button type="link" danger>解绑</a-button>
              </a-popconfirm>
            </template>
            <a-list-item-meta :description="`${item.username} · ${item.relationship}`">
              <template #title>
                <span class="strong">{{ item.nickname }}</span>
              </template>
            </a-list-item-meta>
            <a-tag :color="item.status === 'ACTIVE' ? 'green' : 'orange'">{{ item.status }}</a-tag>
          </a-list-item>
        </template>
        <template #locale-empty-text>
          还没有绑定视障人士
        </template>
      </a-list>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type SelectProps, type TableColumnsType } from 'ant-design-vue';
import type { FormInstance } from 'ant-design-vue/es/form';
import {
  createFamilyMember,
  createFamilyMemberBinding,
  deleteFamilyMember,
  deleteFamilyMemberBinding,
  fetchFamilyBindings,
  fetchFamilyMemberBlindUsers,
  fetchFamilyMembers,
  updateFamilyMember
} from '@/api/familySafety';
import { fetchAppUsers } from '@/api/appUser';
import { formatDateTime } from '@/lib/format';
import type {
  AppUser,
  FamilyBinding,
  FamilyMember,
  FamilyMemberBindingPayload,
  FamilyMemberPayload
} from '@/types/models';

interface FamilyFormState {
  name: string;
  phone: string;
  email: string;
}

interface BindingFormState {
  familyMemberId: number | null;
  userId: number | null;
  relationship: string;
}

const PHONE_PATTERN = /^$|^1\d{10}$/;
const EMAIL_PATTERN = /^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;

const loading = ref(false);
const submitLoading = ref(false);
const bindingSubmitLoading = ref(false);
const drawerLoading = ref(false);
const keyword = ref('');

const familyMembers = ref<FamilyMember[]>([]);
const bindings = ref<FamilyBinding[]>([]);
const blindUsers = ref<AppUser[]>([]);
const drawerBindings = ref<FamilyBinding[]>([]);

const familyModalOpen = ref(false);
const bindingModalOpen = ref(false);
const drawerOpen = ref(false);
const familyEditingId = ref<number | null>(null);
const drawerFamily = ref<FamilyMember | null>(null);

const familyFormRef = ref<FormInstance>();
const bindingFormRef = ref<FormInstance>();

const familyFormState = reactive<FamilyFormState>({
  name: '',
  phone: '',
  email: ''
});

const bindingFormState = reactive<BindingFormState>({
  familyMemberId: null,
  userId: null,
  relationship: ''
});

const familyColumns: TableColumnsType<FamilyMember> = [
  { title: '家属', dataIndex: 'name', key: 'name' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '绑定人数', dataIndex: 'bindingCount', key: 'bindingCount', width: 120 },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 180 },
  { title: '操作', key: 'action', width: 220 }
];

const bindingColumns: TableColumnsType<FamilyBinding> = [
  { title: '家属', dataIndex: 'familyName', key: 'familyName' },
  { title: '绑定用户', key: 'user' },
  { title: '关系', dataIndex: 'relationship', key: 'relationship' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 }
];

const coveredBlindUserCount = computed(() => new Set(bindings.value.map((item) => item.userId)).size);

const familyMemberOptions = computed<SelectProps['options']>(() =>
  familyMembers.value.map((item) => ({
    label: `${item.name}${item.phone ? ` · ${item.phone}` : ''}`,
    value: item.id
  }))
);

const blindUserOptions = computed<SelectProps['options']>(() =>
  blindUsers.value.map((item) => ({
    label: `${item.nickname} · ${item.username}`,
    value: item.id
  }))
);

onMounted(loadAll);

async function loadAll() {
  loading.value = true;
  try {
    const [familyList, bindingList, userList] = await Promise.all([
      fetchFamilyMembers(keyword.value.trim()),
      fetchFamilyBindings(keyword.value.trim()),
      fetchAppUsers()
    ]);
    familyMembers.value = familyList;
    bindings.value = bindingList;
    blindUsers.value = userList.filter((item) => item.status === 1);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载家属管理数据失败');
  } finally {
    loading.value = false;
  }
}

function resetSearch() {
  keyword.value = '';
  loadAll();
}

function openFamilyModal(record?: FamilyMember) {
  familyEditingId.value = record?.id ?? null;
  Object.assign(familyFormState, {
    name: record?.name ?? '',
    phone: record?.phone ?? '',
    email: record?.email ?? ''
  });
  familyModalOpen.value = true;
}

async function handleFamilySubmit() {
  try {
    await familyFormRef.value?.validate();
    submitLoading.value = true;
    const payload = buildFamilyPayload();
    if (familyEditingId.value) {
      await updateFamilyMember(familyEditingId.value, payload);
      message.success('家属信息已更新');
    } else {
      await createFamilyMember(payload);
      message.success('家属已创建');
    }
    familyModalOpen.value = false;
    resetFamilyForm();
    await loadAll();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存家属失败');
  } finally {
    submitLoading.value = false;
  }
}

function handleFamilyCancel() {
  familyModalOpen.value = false;
  resetFamilyForm();
}

async function handleDeleteFamily(record: FamilyMember) {
  try {
    await deleteFamilyMember(record.id);
    message.success('家属已删除');
    if (drawerFamily.value?.id === record.id) {
      drawerOpen.value = false;
      drawerFamily.value = null;
      drawerBindings.value = [];
    }
    await loadAll();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除家属失败');
  }
}

async function openFamilyDrawer(record: FamilyMember) {
  drawerFamily.value = record;
  drawerOpen.value = true;
  drawerLoading.value = true;
  try {
    drawerBindings.value = await fetchFamilyMemberBlindUsers(record.id);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载绑定关系失败');
  } finally {
    drawerLoading.value = false;
  }
}

function openBindingModal(familyMemberId?: number) {
  Object.assign(bindingFormState, {
    familyMemberId: familyMemberId ?? null,
    userId: null,
    relationship: ''
  });
  bindingModalOpen.value = true;
}

async function handleBindingSubmit() {
  try {
    await bindingFormRef.value?.validate();
    bindingSubmitLoading.value = true;
    await createFamilyMemberBinding(bindingFormState.familyMemberId as number, buildBindingPayload());
    message.success('绑定已创建');
    bindingModalOpen.value = false;
    resetBindingForm();
    await loadAll();
    if (drawerOpen.value && drawerFamily.value?.id === bindingFormState.familyMemberId) {
      drawerBindings.value = await fetchFamilyMemberBlindUsers(bindingFormState.familyMemberId as number);
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建绑定失败');
  } finally {
    bindingSubmitLoading.value = false;
  }
}

function handleBindingCancel() {
  bindingModalOpen.value = false;
  resetBindingForm();
}

async function handleUnbind(record: FamilyBinding) {
  try {
    await deleteFamilyMemberBinding(record.familyMemberId, record.userId);
    message.success('已解绑');
    if (drawerFamily.value) {
      drawerBindings.value = await fetchFamilyMemberBlindUsers(drawerFamily.value.id);
    }
    await loadAll();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '解绑失败');
  }
}

function buildFamilyPayload(): FamilyMemberPayload {
  return {
    name: familyFormState.name.trim(),
    phone: familyFormState.phone.trim(),
    email: familyFormState.email.trim()
  };
}

function buildBindingPayload(): FamilyMemberBindingPayload {
  return {
    userId: bindingFormState.userId as number,
    relationship: bindingFormState.relationship.trim(),
    status: 'ACTIVE'
  };
}

function resetFamilyForm() {
  familyEditingId.value = null;
  Object.assign(familyFormState, {
    name: '',
    phone: '',
    email: ''
  });
  familyFormRef.value?.clearValidate();
}

function resetBindingForm() {
  Object.assign(bindingFormState, {
    familyMemberId: null,
    userId: null,
    relationship: ''
  });
  bindingFormRef.value?.clearValidate();
}

function filterOption(input: string, option?: { label?: string | number }) {
  return String(option?.label ?? '').toLowerCase().includes(input.toLowerCase());
}
</script>

<style scoped>
@import '@/styles/ops-surface.css';

.hero {
  background: linear-gradient(135deg, #102542, #1a4b7a 56%, #0f766e 100%);
}

.surface-card :deep(.ant-card-body) {
  padding: 16px 20px;
}

.drawer-header {
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(16, 37, 66, 0.08), rgba(15, 118, 110, 0.08));
}

.drawer-name {
  font-size: 18px;
  font-weight: 700;
  color: #102542;
}

.drawer-meta {
  margin-top: 6px;
  color: #6a788f;
}
</style>
