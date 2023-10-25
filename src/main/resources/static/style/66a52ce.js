(window.webpackJsonp=window.webpackJsonp||[]).push([[28],{464:function(e,t,o){"use strict";o.r(t);var l={layout:"layout",head:function(){return{title:"短信配置"}},data:function(){return{key:"",form:{codeAccessKeyId:"",codeAccessKeySecret:"",codeEndpoint:"",codeTemplate:"",codeSignName:"",isPhone:!1}}},beforeDestroy:function(){},created:function(){},mounted:function(){var e=this;localStorage.getItem("webkey")?e.key=localStorage.getItem("webkey"):(e.$message({message:"认证失效！",type:"error"}),e.$router.push({path:"/"})),e.getConfig()},methods:{save:function(){var e=this,t=e.form;e.form.isPhone,t.isPhone;var data={webkey:e.key,params:JSON.stringify(t)},o=this.$loading({lock:!0,text:"Loading",spinner:"el-icon-loading",background:"rgba(0, 0, 0, 0.7)"});e.$axios.$post(e.$api.apiConfigUpdate(),this.qs.stringify(data)).then((function(t){o.close(),1==t.code?(e.$message({message:t.msg,type:"success"}),e.getConfig()):e.$message({message:t.msg,type:"error"})})).catch((function(t){o.close(),console.log(t),e.$message({message:"接口请求异常，请检查网络！",type:"error"})}))},getConfig:function(){var e=this,data={webkey:e.key};e.$axios.$post(e.$api.getApiConfig(),this.qs.stringify(data)).then((function(t){1==t.code&&(e.form.codeAccessKeyId=t.data.codeAccessKeyId,e.form.codeAccessKeySecret=t.data.codeAccessKeySecret,e.form.codeEndpoint=t.data.codeEndpoint,e.form.codeTemplate=t.data.codeTemplate,e.form.codeSignName=t.data.codeSignName,1==t.data.isPhone?e.form.isPhone=!0:e.form.isPhone=!1)})).catch((function(t){console.log(t),e.$message({message:"接口请求异常，请检查网络！",type:"error"})}))}}},c=o(26),component=Object(c.a)(l,(function(){var e=this,t=e._self._c;return t("div",{staticClass:"page-container"},[t("el-row",{attrs:{gutter:15}},[t("el-col",{attrs:{span:24}},[t("div",{staticClass:"data-box"},[t("div",{staticClass:"page-title"},[t("h4",[e._v("短信配置")]),e._v(" "),t("p",[e._v("目前采用阿里云短信，直接前往阿里云官方开通短信服务即可。参考文档："),t("a",{attrs:{href:"https://developer.aliyun.com/article/252987",target:"_blank"}},[e._v("立即前往")])])]),e._v(" "),t("div",{staticClass:"page-form"},[t("el-form",{ref:"form",attrs:{model:e.form,"label-position":"top","label-width":"80px"}},[t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("是否开启手机号"),t("span",{staticClass:"text-gray"},[e._v("开启后，将新增手机号相关功能")])]),e._v(" "),t("el-switch",{model:{value:e.form.isPhone,callback:function(t){e.$set(e.form,"isPhone",t)},expression:"form.isPhone"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("阿里云短信AccessKeyId")]),e._v(" "),t("el-input",{attrs:{placeholder:"请输入阿里云短信AccessKeyId"},model:{value:e.form.codeAccessKeyId,callback:function(t){e.$set(e.form,"codeAccessKeyId",t)},expression:"form.codeAccessKeyId"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("阿里云短信AccessKeySecret")]),e._v(" "),t("el-input",{attrs:{placeholder:"请输入阿里云短信AccessKeySecret"},model:{value:e.form.codeAccessKeySecret,callback:function(t){e.$set(e.form,"codeAccessKeySecret",t)},expression:"form.codeAccessKeySecret"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("阿里云短信请求地址")]),e._v(" "),t("el-input",{attrs:{placeholder:"请输入阿里云短信请求地址"},model:{value:e.form.codeEndpoint,callback:function(t){e.$set(e.form,"codeEndpoint",t)},expression:"form.codeEndpoint"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("阿里云短信模板")]),e._v(" "),t("el-input",{attrs:{placeholder:"请输入阿里云短信模板ID"},model:{value:e.form.codeTemplate,callback:function(t){e.$set(e.form,"codeTemplate",t)},expression:"form.codeTemplate"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("阿里云短信签名")]),e._v(" "),t("el-input",{attrs:{placeholder:"请输入阿里云短信签名"},model:{value:e.form.codeSignName,callback:function(t){e.$set(e.form,"codeSignName",t)},expression:"form.codeSignName"}})],1),e._v(" "),t("el-form-item",[t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.save()}}},[e._v("保存设置")])],1)],1)],1)])])],1)],1)}),[],!1,null,null,null);t.default=component.exports}}]);