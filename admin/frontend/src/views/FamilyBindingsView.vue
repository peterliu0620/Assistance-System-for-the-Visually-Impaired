<template>
  <div class="ops-page">
    <section class="hero">
      <div>
        <div class="hero-kicker">FAMILY BRIDGE</div>
        <h2>把家属协同关系先绑定好，后续日志和药品档案都会自然归位。</h2>
        <p>第一版采用后台直绑模式，家属能按绑定关系查看核心生活日志并维护对应用户的药品信息。</p>
      </div>
      <div class="hero-actions">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索家属姓名 / 手机 / 用户名"
          allow-clear
          enter-button="搜索"
          @search="loadBindings"
        />
        <a-button type="primary" size="large" @click="openCreateModal">新增绑定</a-button>
      </div>
    </section>

    <section class="stats-grid">
      <div class="stat-card accent">
        <div class="stat-label">绑定总数</div>
        <div class="stat-value">{{ bindings.length }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">家属人数</div>
        <div class="stat-value">{{ familyCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已覆盖用户</div>
        <div class="stat-value">{{ userCount }}</div>
      </div>
    </section>

    <a-card class="surface-card" :bordered="false">
      <template #title>
        <div class="table-headline">
          <div>
            <div class="table-title">绑定列表</div>
            <div class="table-subtitle">查看家属与用户之间的协同关系</div>
          </div>
          <a-tag color="cyan">共 {{ bindings.length }} 条</a-tag>
        </div>
      </template>
      <a-table
        row-key="id"
        :loading="loading"
        :data-source="bindings"
        :columns="columns"
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
      v-model:open="modalOpen"
      title="新增家属绑定"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 17 }">
        <a-form-item label="用户 ID" name="userId" :rules="[{ required: true, message: '请输入用户 ID' }]">
          <a-input-number v-model:value="formState.userId" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="家属姓名" name="familyName" :rules="[{ required: true, message: '请输入家属姓名' }]">
          <a-input v-model:value="formState.familyName" />
        </a-form-item>
        <a-form-item label="手机号" name="familyPhone">
          <a-input v-model:value="formState.familyPhone" />
        </a-form-item>
        <a-form-item label="邮箱" name="familyEmail">
          <a-input v-model:value="formState.familyEmail" />
        </a-form-item>
        <a-form-item label="关系" name="relationship" :rules="[{ required: true, message: '请输入关系' }]">
          <a-input v-model:value="formState.relationship" placeholder="如：女儿、儿子、配偶" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { createFamilyBinding, fetchFamilyBindings } from '@/api/familySafety';
import { formatDateTime } from '@/lib/format';

const loading = ref(false);
const submitLoading = ref(false);
const modalOpen = ref(false);
const bindings = ref([]);
const keyword = ref('');
const formRef = ref();

const formState = reactive({
  userId: null,
  familyName: '',
  familyPhone: '',
  familyEmail: '',
  relationship: ''
});

const columns = [
  { title: '家属', dataIndex: 'familyName', key: 'familyName' },
  { title: '绑定用户', key: 'user' },
  { title: '关系', dataIndex: 'relationship', key: 'relationship' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 }
];

const familyCount = computed(() => new Set(bindings.value.map((item) => item.familyMemberId)).size);
const userCount = computed(() => new Set(bindings.value.map((item) => item.userId)).size);

onMounted(loadBindings);

async function loadBindings() {
  loading.value = true;
  try {
    bindings.value = await fetchFamilyBindings(keyword.value.trim());
  } finally {
    loading.value = false;
  }
}

function openCreateModal() {
  modalOpen.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();
  submitLoading.value = true;
  try {
    await createFamilyBinding({ ...formState, status: 'ACTIVE' });
    message.success('绑定已创建');
    modalOpen.value = false;
    resetForm();
    loadBindings();
  } finally {
    submitLoading.value = false;
  }
}

function handleCancel() {
  modalOpen.value = false;
  resetForm();
}

function resetForm() {
  Object.assign(formState, {
    userId: null,
    familyName: '',
    familyPhone: '',
    familyEmail: '',
    relationship: ''
  });
  formRef.value?.clearValidate();
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
</style>
