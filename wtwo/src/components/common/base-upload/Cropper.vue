<template>
    <div class="main">
        <div class="cropper-container">
      <div class="cropper-left">
        <div class="cropper-content">
          <VueCropper
            ref="cropperRef"
            :img="props.imgObj?.url || props.url"
            :outputSize="option.outputSize"
            :outputType="option.outputType"
            :info="option.info"
            :full="option.full"
            :canMove="option.canMove"
            :canMoveBox="option.canMoveBox"
            :fixedBox="option.fixedBox"
            :original="option.original"
            :autoCrop="option.autoCrop"
            :autoCropWidth="option.autoCropWidth"
            :autoCropHeight="option.autoCropHeight"
            :centerBox="option.centerBox"
            :high="option.high"
            :infoTrue="option.infoTrue"
            :enlarge="option.enlarge"
            :fixed="option.fixed"
            :fixedNumber="option.fixedNumber"
            :mode="props.mode"
            @realTime="realTime"
          />
        </div>
      </div>
      <div class="cropper-right">
        <div :style="previews?.div" class="avatar-upload-preview">
          <img :src="previews?.url" :style="previews?.img" />
        </div>
      </div>
    </div>
        <div class="cropper-footer">
          <el-tooltip class="item" effect="dark" content="放大" placement="top">
            <el-button :icon="ZoomIn" type="primary" circle @click="onCropperzoom(1)" />
          </el-tooltip>
          <el-tooltip class="item" effect="dark" content="缩小" placement="top">
            <el-button :icon="ZoomOut" type="primary" circle @click="onCropperzoom(-1)" />
          </el-tooltip>
          <el-tooltip class="item" effect="dark" content="逆时针旋转" placement="top">
            <el-button :icon="RefreshLeft" type="primary" circle @click="onRotateLeft" />
          </el-tooltip>
          <el-tooltip class="item" effect="dark" content="顺时针旋转" placement="top">
            <el-button :icon="RefreshRight" type="primary" circle @click="onRotateRight" />
          </el-tooltip>
          <el-button type="primary" @click="onSave">保存</el-button>
          <!-- <el-button type="info" @click="onCancle">取消</el-button> -->
        </div>
    </div>
    
  </template>
  <script setup>
  import 'vue-cropper/dist/index.css'
  import { VueCropper } from 'vue-cropper'
  import { ref } from 'vue'
  import { assignIn, cloneDeep } from 'lodash'
  import {
    ZoomIn,
    ZoomOut,
    RefreshLeft,
    RefreshRight
  } from '@element-plus/icons-vue'
  const cropperRef = ref()
  const props = defineProps({
    config: {
      type: Object,

      default: () => { }
    },
    url: { // 图片路径，支持url 地址, base64, blob
      type: [String, Object],
      default: () => null
    },
    imgObj: { // 图片信息
      type: Object,
      default: () => null
    }
  })
  const option = ref({
    size: 1,
    outputType: 'jpeg || png || webp', // 裁剪生成图片的格式
    outputSize: 1, // 裁剪生成图片的质量
    full: true, // 输出原图比例截图 props名full
    autoCrop: true, //    是否默认生成截图框
    canMove: true, // 上传图片是否可以移动
    canMoveBox: true, // 截图框能否拖动
    fixedBox: false, // 固定截图框大小 不允许改变
    original: false, // 上传图片按照原始比例渲染
    autoCropWidth: 300, // 默认生成截图框宽度
    autoCropHeight: 300, // 默认生成截图框高度
    centerBox: true, // 截图框是否被限制在图片里面
    high: true, // 是否按照设备的dpr 输出等比例图片
    infoTrue: true, // true 为展示真实输出图片宽高 false 展示看到的截图框宽高
    enlarge: 1, // 图片根据截图框输出比例倍数
    maxImgSize: 2000, // 限制图片最大宽度和高度
    fixed: false, // 是否开启截图框宽高固定比例
    fixedNumber: [4, 4],
    info: true,
    mode: '100%'
  })
  option.value = cloneDeep({...option.value, ...props.config})
  
  const previews = ref(null)
  const emits = defineEmits(['confirm', 'cancle'])
  /**
   * 取消
   */
  const onCancle = () => {
    emits('cancle', null)
  }
  /**
   * 保存
   */
  const onSave = (e) => {
    cropperRef.value.getCropBlob((blob) => {
      const newDate = Date.now()
      const fileName = `cropper${newDate}.`+encodeURIComponent(props.imgObj?props.imgObj.name:'a.png').split(".").pop()
      const raw = new File([blob], fileName, { type: 'image/png', lastModified: newDate })
      raw.uid = newDate
      const url = window.URL.createObjectURL(blob)
      const newImgObj = {
        name: fileName,
        url: url,
        raw,
        status: props.imgObj?.status || 'ready',
        percentage: props.imgObj?.percentage || 0,
        size: raw.size,
        uid: raw.uid
      }
      emits('confirm', newImgObj)
    })
  }
  // 缩放
  const onCropperzoom = (num = 1) => {
    cropperRef.value.changeScale(num)
  }
  
  // 左旋转
  const onRotateLeft = () => {
    cropperRef.value.rotateLeft()
  }
  /**
       * 右旋转
       */
  const onRotateRight = () => {
    cropperRef.value.rotateRight()
  }
  /**
     * 实时预览事件
     */
  const realTime = (res) => {
    previews.value = res
  }
  /**
   *  图片加载的回调, 返回结果 success, error
   */
  // const imgLoad = () => { }
  </script>
  <style lang="scss" scoped>
  .main{
    .cropper-footer {
        border-top: 1px solid #ebeef5;
        padding: 10px;
    //   @include flexLayout();
        display: flex;
        justify-content: end;
    }
  }
  .cropper-container {
    // @include flexLayout($horizontal: space-between);
    display: flex;
    justify-content: space-between;
    width: 100%;
    .cropper-left,
    .cropper-right {
      width: 50%;
      min-width: 300px;
    }
    .cropper-content {
      height: 400px;
    }
    .cropper-right {
    //   @include flexLayout($horizontal: center, $direction: column, $vertical: center);
    display: flex;
    justify-content: center;
    flex-direction: column;
    align-items: center;
    }
    .avatar-upload-preview {
      overflow: hidden;
    }
  }
  </style>