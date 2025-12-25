<template>
  <div class="upload-container">
    <div class="upload-content">
      <el-upload
        :class="{ limit: props.limit === fileLists.length }"
        class="upload-file"
        :file-list="fileArr"
        ref="uploadRef"
        action="#"
        list-type="picture-card"
        :multiple="multiple"
        :accept="accept"
        :disabled="disabled"
        :auto-upload="false"
        :limit="limit"
        :on-change="onChange"
        :on-exceed="onExceed"
      >
        <el-icon><Plus /></el-icon>
        <template #file="{ file }">
          <template v-if="props.accept.search('image') > -1">
            <div>
              <img
                class="wtwo-upload-list__item-thumbnail"
                :src="file.url"
                alt=""
              />
              <span class="wtwo-upload-list__item-actions">
                <span
                  class="wtwo-upload-list__item-preview"
                  @click="onImgPreview(file)"
                >
                  <el-icon><zoom-in /></el-icon>
                </span>
                <!-- <template v-if="props.shear">
                    <span class="wtwo-upload-list__item-preview" @click="onCropper(file)">
                      <el-icon><Crop /></el-icon>
                    </span>
                  </template> -->
                <span
                  v-if="!disabled"
                  class="wtwo-upload-list__item-delete"
                  @click="onRemove(file)"
                >
                  <el-icon><Delete /></el-icon>
                </span>
              </span>
            </div>
          </template>
          <template v-else>
            <div class="annex-content">
              <template v-if="file.name.search('.docx') > -1">
                <i class="annex-word icon-word2 iconfont"></i>
              </template>
              <template v-if="file.name.search('.pdf') > -1">
                <i class="annex-word icon-format-pdf iconfont"></i>
              </template>
              {{ file.name }}
              <span class="wtwo-upload-list__item-actions">
                <span
                  v-if="!disabled"
                  class="wtwo-upload-list__item-delete"
                  @click="onRemove(file, e, c)"
                >
                  <el-icon><Delete /></el-icon>
                </span>
              </span>
            </div>
          </template>
        </template>
        <template #tip>
          <div
            class="upload-tip"
            v-if="tip && props.limit !== fileLists.length"
          >
            {{ tip }}
          </div>
        </template>
      </el-upload>
    </div>
    <template v-if="require">
      <div class="error-tip" v-if="!props.vaild">{{ message }}</div>
    </template>
    <template v-if="isExceed">
      <div class="error-tip">超出范围,请重新上传</div>
    </template>
    <el-image-viewer
      v-if="dialogVisible"
      :zoom-rate="1.2"
      :initial-index="previewIndex"
      @close="closePreview"
      :url-list="imgPreviewList"
    />
    <el-dialog
      v-model="shearVisible"
      title="剪切图片"
      :show-close="true"
      width="800"
      :before-close="onCropperCancle"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <Cropper
        :imgObj="currentImgObj"
        :config="props.shearConfig"
        @cancle="shearVisible=false"
        @confirm="onCropperSave"
      />
    </el-dialog>
  </div>
</template>
  <script setup>
import { onMounted, ref, watch } from "vue";
import { ElMessage, ElImageViewer } from "element-plus";
import { Delete, Plus, ZoomIn, Crop } from "@element-plus/icons-vue";
import { cloneDeep } from "lodash";
import Cropper from "./Cropper";
import { uploadFile } from "@/services/oss";

const props = defineProps({
  shear: {
    //  是否开启上传剪切
    type: Boolean,
    default: () => false,
  },
  shearConfig: {
    //  剪切配置，具体配置查看cropper 组件
    type: Object,
    default: () => {},
  },
  vaild: {
    //  默认图片校验通过
    type: Boolean,
    default: () => true,
  },
  require: {
    //  是否是必填项
    type: Boolean,
    default: () => true,
  },
  message: {
    //  不为空提示
    type: String,
    default: () => "文件不能为空",
  },
  tip: {
    //  tip 提示
    type: String,
    default: () => "",
  },
  fileLists: {
    type: Array,
    default: () => [],
  },
  limit: {
    //  多图上传，最多允许几张图片
    type: Number,
    default: () => 2,
  },
  multiple: {
    type: Boolean,
    default: () => true,
  },
  accept: {
    type: String,
    default: () => "image/*",
  },
  showFileList: {
    type: Boolean,
    default: () => false,
  },
  drag: {
    type: Boolean,
    default: () => true,
  },
  maxSize: {
    //  最大size,10M
    type: Number,
    default: () => 10,
  },
  disabled: {
    // 是否禁止
    type: Boolean,
    default: () => false,
  },
});
const fileArr = ref([]);
const imgValid = ref(true); //  图片校验是否通过
const uploadRef = ref(null);
const dialogVisible = ref(false);
const emits = defineEmits(["update:fileLists"]);
const shearVisible = ref(false); // 剪切弹框
const currentImgObj = ref(null); // 当前上传的图片
const isExceed = ref(false);
const fileCropperArr = ref([]); // 剪切的图
const imgPreviewList = ref([]);
const previewIndex = ref(0);
watch(
  () => props.fileLists,
  (newV, oldV) => {
    fileArr.value = cloneDeep(props.fileLists);
    imgValid.value = fileArr.value.length > 0;
  },
  { immediate: true, deep: true }
);
onMounted(() => {
  fileCropperArr.value = [];
});
/**
 * 清空剪切的数组
 */
const clearShearArr = () => {
  fileCropperArr.value = [];
};
defineExpose({ clearShearArr });
/**
 * 剪切保存
 */
const onCropperSave = (res) => {
  const loading = ElLoading.service({
    lock: true,
    text: "加载中",
  });
  uploadFile(res.raw, { fileType: 1 })
    .then((response) => {
      const newFileArr = cloneDeep(fileArr.value);
      if(!props.multiple && props.limit.length < 2){
        fileArr.value = [];
      }
      
      res.url = response.url;
      //   const reg = /blob:(\S*)/;
      //   for (const item of newFileArr) {
      //     if (!reg.test(item.url)) {
      //       // 服务器图片,代表着图片没有变化
      //       fileArr.value.push(item);
      //     }
      //   }
      fileCropperArr.value.push(res);
      console.log("fileCropperArrSave=", fileCropperArr.value);
      shearVisible.value = false;
      for (const item of fileCropperArr.value) {
        const newDate = Date.now();
        const raw = new File([item.raw], item.name, {
          type: "image/png",
          lastModified: newDate,
        });
        raw.uid = item.uid;

        const newImgObj = {
          name: item.name,
          url: item.url,
          raw,
          status: props.imgObj?.status || "ready",
          percentage: props.imgObj?.percentage || 0,
          size: raw.size,
          uid: raw.uid,
        };
        fileArr.value.push(newImgObj);
      }
      exportList();
      clearShearArr()
    })
    .finally(() => {
      loading.close();
    });
};
/**
 * 打开剪切
 */
const onCropper = (file) => {
  // currentImgObj.value = file
  shearVisible.value = true;
};
const onCropperCancle = () => {
  exportList();
  shearVisible.value = false;
  // uploadFileApi(currentImgObj.value).then(res=>{
  //   shearVisible.value = false;
  // })
};
/**
 * 超出范围
 */
const onExceed = () => {
  isExceed.value = true;
  setTimeout(() => {
    isExceed.value = false;
  }, 3000);
};
/**
 * 预览
 */
const onImgPreview = (file) => {
  const reg = /blob:(\S*)/;
  let index;
  if (file.uid) {
    if (!reg.test(file.url)) {
      // 服务器图片   这个暂时没设置uid
      index = fileArr.value.findIndex((item) => item.uid === file.uid);
    } else {
      //  上传的图片以blob: 开头
      index = fileArr.value.findIndex((item) => item.uid === file.uid);
    }
  } else {
    index = fileArr.value.findIndex((item) => item.url === file.url);
  }
  previewIndex.value = index;
  imgPreviewList.value = fileArr.value.map((item) => item.url);
  dialogVisible.value = true;
};

const closePreview = () => {
  imgPreviewList.value = [];
  dialogVisible.value = false;
};
/**
 * 删除
 */
const onRemove = (file) => {
  const reg = /blob:(\S*)/;
  let index;
  if (file.uid) {
    if (!reg.test(file.url)) {
      // 服务器图片   这个暂时没设置uid
      index = fileArr.value.findIndex((item) => item.uid === file.uid);
    } else {
      //  上传的图片以blob: 开头
      index = fileArr.value.findIndex((item) => item.uid === file.uid);
    }
  } else {
    index = fileArr.value.findIndex((item) => item.url === file.url);
  }
  if (index > -1) {
    // 删除
    fileArr.value.splice(index, 1);
  }
  // console.log('删除', index)
  if (props.shear) {
    // 剪切
    const sIndex = fileCropperArr.value.findIndex(
      (item) => item.uid === file.uid
    );
    // console.log('剪切的图片删除', sIndex)
    if (sIndex > -1) {
      fileCropperArr.value.splice(sIndex, 1);
    }
  }
  // if (reg.test(file.url)) { // 上传的图片
  //   if (!props.shear) {
  //     uploadRef.value.handleRemove(file)
  //   }
  // }
  // console.log('fileCropperArrDel=', fileCropperArr)
  exportList();
};

const beforeAvatarUpload = (rawFile) => {
  const allowedTypes = ["image/jpeg", "image/png"];
  const isLt2M = rawFile.size / 1024 / 1024 < props.maxSize;
  if (!isLt2M) {
    ElMessage.error(`只能上传小于${props.maxSize}M的文件`);
    return;
  }
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error("请上传jpg、png格式的图片!");
    return false;
  } else if (!isLt2M) {
    ElMessage.error(`只能上传小于${props.maxSize}M的文件`);
    return false;
  }
  return true;
};

function uploadFileApi(file){
  const loading = ElLoading.service({
      lock: true,
      text: "加载中",
    });
    return uploadFile(file.raw, { fileType: 1 })
      .then((res) => {
        file.url = res.url;
        fileArr.value.push(file);
        exportList();
      })
      .finally(() => {
        loading.close();
      });
}
/**
 * 上传
 */
const onChange = (file, fileList) => {
  if (!beforeAvatarUpload(file.raw)) {
    fileArr.value = cloneDeep(fileArr.value);
    return false;
  }

  if (props.shear && props.accept.search("image") > -1) {
    // 开启剪切仅支持图片剪切
    shearVisible.value = true;
    currentImgObj.value = file;
  } else {
    uploadFileApi(file)
  }
};
/**
 * 抛出list
 */
const exportList = () => {
  console.log("fileArr", fileArr.value);

  imgValid.value = fileArr.value.length > 0;
  console.log("exportList", fileArr.value, imgValid.value);

  emits("update:fileLists", fileArr.value);
  emits("update:vaild", imgValid.value);
};
const bufToFile = (buf, filename) => {
  return new File([buf], filename, {
    type: "image/png",
    lastModified: Date.now(),
  });
};
</script>
  <style lang="scss" scoped>
  :deep(.wtwo-upload--picture-card){
    width: 100px !important;
    height: 100px !important;
  }
.upload-container {
  .upload-content {
    display: flex;
    align-items: flex-end;
    .upload-tip {
      color: #909399;
      font-size: 12px;
      margin-left: 5px;
      margin-top: 5px;
      line-height: 16px;
    }
    .upload-file {
      &.no-multiple,
      &.limit {
        :deep(.wtwo-upload-list__item ~ .wtwo-upload) {
          display: none;
        }
      }
    }
  }
  .error-tip {
    font-size: 12px;
    color: #f56c6c;
    margin-top: 10px;
  }
  .annex-content {
    margin-top: 14px;
    font-size: 20px;
    .annex-word {
      font-size: 24px;
      color: #409eff;
    }
  }
}
:deep(.wtwo-dialog__body) {
  padding: 0;
  font-size: 0;
}
:deep(.wtwo-upload-list__item){
  width: 100px;
  height: 100px;
}
</style>
  