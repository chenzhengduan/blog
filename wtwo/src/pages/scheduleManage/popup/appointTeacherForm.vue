<template>
  <div>
    <el-drawer
      v-model="isShow"
      :before-close="close"
      show-close
      size="1150px"
      :destroy-on-close="true"
      :title="transToConfigDescript(type == 'edit' ? '编辑老师' : '添加老师')"
    >
      <div class="p-16px" v-loading="loading">
        <el-form
          ref="ruleFormRef"
          :model="ruleForm"
          :rules="rules"
          label-width="auto"
          class="demo-ruleForm"
          status-icon
        >
          <el-form-item :label="transToConfigDescript('老师姓名')" prop="">
            <input-tag
              placeholder="请选择"
              :selected="teacherInfo.Name? [teacherInfo] : []"
              :isLine="true"
              :multiple="false"
              @click.native="selectEmp()"
              @change="handleTeacherChange"
              :disabled="type == 'edit'"
            >
              <template #btn-icon>
                  <el-icon size="18px">
                      <svg aria-hidden="true">
                          <use
                              xlink:href="#w2-xuanren"
                          ></use>
                      </svg>
                  </el-icon>
              </template>
            </input-tag>
          </el-form-item>
          <el-form-item label="科目" prop="Subject">
            <el-select
              v-model="ruleForm.Subject"
              multiple
              filterable
              placeholder="请选择科目"
              style="width: 100%"
            >
              <el-option
                v-for="item in subjectList"
                :key="item.ID"
                :label="item.Value"
                :value="item.ID"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="头像" prop="Image">
            <div>
              <Upload
                :limit="1"
                tip=""
                :shear="true"
                :require="true"
                :shearConfig="{
                  fixed: true,
                  autoCropWidth: 118,
                  autoCropHeight: 118,
                }"
                :multiple="false"
                v-model:fileLists="ruleForm.Image"
                v-model:vaild="imgValid"
                ref="uploadRef"
                message=""
              />
              <span class="text-xs color-#848484">尺寸建议：118x118</span>
            </div>
          </el-form-item>
          <el-form-item label="内容" prop="Content">
            <div class="editor-box">
              <Toolbar
                :editor="editorRef"
                :defaultConfig="toolbarConfig"
                mode="default"
                style="border-bottom: 1px solid #dcdfe6"
              />
              <Editor
                :defaultConfig="editorConfig"
                mode="default"
                v-model="ruleForm.Content"
                style="height: 400px; overflow-y: hidden"
                @onCreated="handleCreated"
              />
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer="">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-drawer>
    <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, shallowRef, computed, watch } from "vue";
import Upload from "@/components/common/base-upload/base-upload.vue";
import { useDictFieldsStore } from '@/store/dict';
import { storeToRefs } from "pinia";
import "@wangeditor/editor/dist/css/style.css";
import { cloneDeep } from "lodash";
import { uploadFile } from "@/services/oss";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";
import { FormRules, FormInstance } from "element-plus";
import { updateTeacherProfile } from "@/api/arrange";
import { transToConfigDescript } from "@/utils/filters/filters";
const isShow = ref(false);
const loading = ref(false);
const ID = ref("");
const imgValid = ref(true);
const chooseEmpTableRef:any = ref<HTMLDivElement | null>(null);
const editorRef = shallowRef();
const ruleFormRef = ref<FormInstance>()
const dictStore = useDictFieldsStore();
interface TeacherInfo {
  Name: string;
  NickName: string;
  ID: string;
}
const initTeacherInfo = {
    Name: '',
    NickName: '',
    ID:''
}
const initRuleForm = {
  Name: '',
  Subject: [],
  Image: [],
  Content: "",
}
const teacherInfo:any = ref<TeacherInfo>(cloneDeep(initTeacherInfo));
const { dictFields } = storeToRefs(dictStore);
const toolbarConfig = {};
const type = ref("add");
const defaultIagme = "https://cdn01.xiaogj.com/file/mall/default/teacher.png";
const subjectList = computed(() => {
  return dictFields.value("SUBJECT");
});

type InsertFnType = (url: string, alt: string, href: string) => void;
const editorConfig = {
  placeholder: "请输入内容",
  MENU_CONF: {
    'uploadImage': {
      async customUpload(file: File, insertFn: InsertFnType) {
        const res = await uploadFile(file, {
          fileType: 1,
        });
        insertFn(res.url, res.url, res.url);
      },
    },
    'uploadVideo':{
            async customUpload(file: File, insertFn: InsertFnType){
                const res = await uploadFile(file, {
                    fileType: 3
                })
                insertFn(res.url, res.url, res.url)
            }
        }
  },
};
interface RuleForm {
  Name: string;
  Image: Array<any>;
  Subject: Array<string>;
}
const ruleForm:any = ref(cloneDeep(initRuleForm));
const rules = reactive<FormRules<RuleForm>>({
  Name: [{ required: true, message: transToConfigDescript("请选择老师"), trigger: "blur" }],
  Subject: [
    {
      required: true,
      message: "请选择科目",
      trigger: "change",
    },
  ],
  // Image: [
  //   {
  //     required: true,
  //     message: "请上传图片",
  //     trigger: "change",
  //   },
  // ],
});
function selectEmp() {
  if(type.value == 'add'){
    chooseEmpTableRef.value?.open({
      multi:false
    }).then((res: any) => {
      if (res.data) {
        teacherInfo.value =res.data?res.data: {}
        ruleForm.value.Name = res.data.Name
      }
    });
  }  
}

function handleTeacherChange(selectedItems: any[]) {
  if (selectedItems.length === 0) {
    // 删除了老师
    teacherInfo.value = cloneDeep(initTeacherInfo)
    ruleForm.value.Name = ''
    ruleFormRef.value?.validateField('Name')
  }
}

function handleCreated(editor: any) {
  editorRef.value = editor; // 记录 editor 实例，重要！
  // console.log(editor.getAllMenuKeys())
}

const submit = () => {
  ruleFormRef.value?.validate((valid:any, fields:any) => {
//     InfoID: "00000000-0000-0000-0000-000000000000",
//   TypeID: "00000000-0000-0000-0000-000000000000", // 栏目ID
    if (valid) {
        const params:any = {
            name: ruleForm.value.Name,
            id:teacherInfo.value.ID,
            subjectIds: ruleForm.value.Subject.join(","),
            image: ruleForm.value.Image.length>0 ? ruleForm.value.Image[0].url: '',
            summary: ruleForm.value.Content,  
        }
        if(type.value == 'add'){
          params.isInsert =  1;
        }
        loading.value = true
        updateTeacherProfile(params).then((res: any) => {
            if (res.ErrorCode == 200) {
              ElMessage.success(type.value == "add" ? "添加成功" : "修改成功");
              _resolve();
              close();
            }
        }).finally(() => {
            loading.value = false
        })
    }
  });
};

function close() {
  if (!ruleFormRef.value) return
  ruleFormRef.value.resetFields()
  teacherInfo.value = cloneDeep(initTeacherInfo)
  ruleForm.value = cloneDeep(initRuleForm)
  loading.value = false;
  isShow.value = false;
}
function open(params: any) {
  console.log(params);
  type.value = params.type;
  if (params.type == 'edit') {
    ID.value = params.ID;
    teacherInfo.value = {
        ID: params.ID,
        NickName: params.NickName,
        Name:params.Name
    }
    ruleForm.value.Name  = params.Name
    ruleForm.value.Subject = params?.SubjectList.map((item:any)=>item.ID) || []
    ruleForm.value.Image = params.Image?[{
        url: params.Image
    }]:[]
    ruleForm.value.Content = params.Summary
  }else{
    ruleForm.value.Image = []
  }
  isShow.value = true;
  return new Promise((resolve, reject) => {
    _resolve = resolve;
    _reject = reject;
  });
}

/** 对外暴露一个open方法 */
let _resolve = null as any,
  _reject = null;

defineExpose({
  open,
});
</script>

<style lang="scss" scoped>
.editor-box{
        border: 1px solid #dcdfe6;
        border-radius: 3px;
        width: 100%;
        overflow: hidden
    }
:deep(.el-drawer__footer){
    border-top: 1px solid #E4E7ED;
}
</style>