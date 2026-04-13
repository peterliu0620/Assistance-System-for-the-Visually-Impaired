<template>
  <div class="ops-page">
    <section class="hero medicine-hero">
      <div>
        <div class="hero-kicker">MEDICINE SAFETY</div>
        <h2>把药品档案提前录好，用户扫描命中后就能优先播报用法、禁忌和有效期提醒。</h2>
        <p>这里重点维护名称、别名、说明摘要和有效期，前端识别命中后会自动切到药品优先播报与强预警模式。</p>
      </div>
      <div class="hero-actions">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索药品名称 / 别名"
          allow-clear
          enter-button="搜索"
          @search="loadProfiles"
        />
        <a-button size="large" @click="resetSearch">重置</a-button>
        <a-button type="primary" size="large" @click="openCreateModal">新增档案</a-button>
      </div>
    </section>

    <a-card class="surface-card" :bordered="false">
      <template #title>
        <div class="table-headline">
          <div>
            <div class="table-title">药品档案列表</div>
            <div class="table-subtitle">维护扫描命中后优先播报的药品资料与有效期信息</div>
          </div>
          <a-tag color="orange">共 {{ profiles.length }} 条</a-tag>
        </div>
      </template>
      <a-table
        row-key="id"
        :loading="loading"
        :data-source="profiles"
        :columns="columns"
        :pagination="{ pageSize: 8, showSizeChanger: false }"
        :scroll="{ x: 1180 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'medicineName'">
            <div>
              <div class="strong">{{ record.medicineName }}</div>
              <div class="muted">{{ record.genericName || '未填写通用名' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'user'">
            <div>
              <div class="strong">{{ record.nickname }}</div>
              <div class="muted">{{ record.username }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'expiryDate'">
            <a-tag :color="isExpired(record.expiryDate) ? 'red' : 'green'">
              {{ record.expiryDate || '未填写' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-row">
              <a-button type="link" @click="openEditModal(record)">编辑</a-button>
              <a-popconfirm title="确认删除该药品档案吗？" @confirm="handleDelete(record)">
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalOpen"
      :title="modalMode === 'create' ? '新增药品档案' : '编辑药品档案'"
      width="760px"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="formState" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="用户 ID" name="userId" :rules="[{ required: true, message: '请输入用户 ID' }]">
          <a-input-number v-model:value="formState.userId" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="家属 ID" name="familyMemberId">
          <a-input-number v-model:value="formState.familyMemberId" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="药品名称" name="medicineName" :rules="[{ required: true, message: '请输入药品名称' }]">
          <a-input v-model:value="formState.medicineName" />
        </a-form-item>
        <a-form-item label="通用名" name="genericName">
          <a-input v-model:value="formState.genericName" />
        </a-form-item>
        <a-form-item label="用法用量" name="dosageUsage">
          <a-textarea v-model:value="formState.dosageUsage" :rows="3" />
        </a-form-item>
        <a-form-item label="适用人群" name="suitablePeople">
          <a-textarea v-model:value="formState.suitablePeople" :rows="2" />
        </a-form-item>
        <a-form-item label="禁忌" name="contraindications">
          <a-textarea v-model:value="formState.contraindications" :rows="2" />
        </a-form-item>
        <a-form-item label="说明摘要" name="description">
          <a-textarea v-model:value="formState.description" :rows="2" />
        </a-form-item>
        <a-form-item label="有效期" name="expiryDate">
          <a-date-picker v-model:value="formState.expiryDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="别名/条码" name="barcodeOrAlias">
          <a-input v-model:value="formState.barcodeOrAlias" placeholder="多个别名可用逗号分隔" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { createMedicineProfile, deleteMedicineProfile, fetchMedicineProfiles, updateMedicineProfile } from '@/api/familySafety';

const loading = ref(false);
const submitLoading = ref(false);
const modalOpen = ref(false);
const modalMode = ref('create');
const currentId = ref(null);
const keyword = ref('');
const profiles = ref([]);
const formRef = ref();

const formState = reactive({
  userId: null,
  familyMemberId: null,
  medicineName: '',
  genericName: '',
  description: '',
  dosageUsage: '',
  suitablePeople: '',
  contraindications: '',
  expiryDate: undefined,
  barcodeOrAlias: ''
});

const columns = [
  { title: '药品', key: 'medicineName', width: 180 },
  { title: '所属用户', key: 'user', width: 160 },
  { title: '用法用量', dataIndex: 'dosageUsage', key: 'dosageUsage', width: 220 },
  { title: '适用人群', dataIndex: 'suitablePeople', key: 'suitablePeople', width: 180 },
  { title: '禁忌', dataIndex: 'contraindications', key: 'contraindications', width: 180 },
  { title: '有效期', dataIndex: 'expiryDate', key: 'expiryDate', width: 120 },
  { title: '别名/条码', dataIndex: 'barcodeOrAlias', key: 'barcodeOrAlias', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 120 }
];

onMounted(loadProfiles);

async function loadProfiles() {
  loading.value = true;
  try {
    profiles.value = await fetchMedicineProfiles({ keyword: keyword.value.trim() || undefined });
  } finally {
    loading.value = false;
  }
}

function resetSearch() {
  keyword.value = '';
  loadProfiles();
}

function openCreateModal() {
  modalMode.value = 'create';
  currentId.value = null;
  resetForm();
  modalOpen.value = true;
}

function openEditModal(record) {
  modalMode.value = 'edit';
  currentId.value = record.id;
  Object.assign(formState, {
    userId: record.userId,
    familyMemberId: record.familyMemberId,
    medicineName: record.medicineName,
    genericName: record.genericName,
    description: record.description,
    dosageUsage: record.dosageUsage,
    suitablePeople: record.suitablePeople,
    contraindications: record.contraindications,
    expiryDate: record.expiryDate,
    barcodeOrAlias: record.barcodeOrAlias
  });
  modalOpen.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();
  submitLoading.value = true;
  try {
    const payload = { ...formState };
    if (modalMode.value === 'create') {
      await createMedicineProfile(payload);
      message.success('药品档案已创建');
    } else {
      await updateMedicineProfile(currentId.value, payload);
      message.success('药品档案已更新');
    }
    modalOpen.value = false;
    resetForm();
    loadProfiles();
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(record) {
  await deleteMedicineProfile(record.id);
  message.success('药品档案已删除');
  loadProfiles();
}

function handleCancel() {
  modalOpen.value = false;
  resetForm();
}

function resetForm() {
  Object.assign(formState, {
    userId: null,
    familyMemberId: null,
    medicineName: '',
    genericName: '',
    description: '',
    dosageUsage: '',
    suitablePeople: '',
    contraindications: '',
    expiryDate: undefined,
    barcodeOrAlias: ''
  });
  formRef.value?.clearValidate();
}

function isExpired(date) {
  return !!date && new Date(date).getTime() < Date.now() - 24 * 60 * 60 * 1000;
}
</script>

<style scoped>
@import '@/styles/ops-surface.css';

.medicine-hero {
	background: linear-gradient(135deg, #40210f, #9a4d1a 55%, #c96a1d 100%);
}
</style>
