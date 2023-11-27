(window.webpackJsonp=window.webpackJsonp||[]).push([[26],{462:function(e,t,o){"use strict";o.r(t);var l={layout:"layout",head:function(){return{title:"经验设置"}},data:function(){return{key:"",form:{clockExp:"",reviewExp:"",postExp:"",violationExp:"",deleteExp:"",spaceMinExp:"",chatMinExp:""}}},beforeDestroy:function(){},created:function(){},mounted:function(){var e=this;localStorage.getItem("webkey")?e.key=localStorage.getItem("webkey"):(e.$message({message:"认证失效！",type:"error"}),e.$router.push({path:"/"})),e.getConfig()},methods:{save:function(){var e=this,t=e.form,data={webkey:e.key,params:JSON.stringify(t)},o=this.$loading({lock:!0,text:"Loading",spinner:"el-icon-loading",background:"rgba(0, 0, 0, 0.7)"});e.$axios.$post(e.$api.apiConfigUpdate(),this.qs.stringify(data)).then((function(t){o.close(),1==t.code?(e.$message({message:t.msg,type:"success"}),e.getConfig()):e.$message({message:t.msg,type:"error"})})).catch((function(t){o.close(),console.log(t),e.$message({message:"接口请求异常，请检查网络！",type:"error"})}))},getConfig:function(){var e=this,data={webkey:e.key};e.$axios.$post(e.$api.getApiConfig(),this.qs.stringify(data)).then((function(t){1==t.code&&(e.form.clockExp=t.data.clockExp,e.form.reviewExp=t.data.reviewExp,e.form.postExp=t.data.postExp,e.form.violationExp=t.data.violationExp,e.form.deleteExp=t.data.deleteExp,e.form.spaceMinExp=t.data.spaceMinExp,e.form.chatMinExp=t.data.chatMinExp)})).catch((function(t){console.log(t),e.$message({message:"接口请求异常，请检查网络！",type:"error"})}))}}},r=o(26),component=Object(r.a)(l,(function(){var e=this,t=e._self._c;return t("div",{staticClass:"page-container"},[t("el-row",{attrs:{gutter:15}},[t("el-col",{attrs:{span:24}},[t("div",{staticClass:"data-box"},[t("div",{staticClass:"page-title"},[t("h4",[e._v("经验设置")]),e._v(" "),t("p",[e._v("设置用户各项操作时奖励或者扣除的经验，对所有模块均生效。")])]),e._v(" "),t("div",{staticClass:"page-form"},[t("el-form",{ref:"form",attrs:{model:e.form,"label-position":"top","label-width":"80px"}},[t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("签到经验"),t("span",[e._v("必须为整数，小于1则无经验")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入签到经验"},model:{value:e.form.clockExp,callback:function(t){e.$set(e.form,"clockExp",t)},expression:"form.clockExp"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("评论经验"),t("span",[e._v("必须为整数，小于1则无经验")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入评论经验"},model:{value:e.form.reviewExp,callback:function(t){e.$set(e.form,"reviewExp",t)},expression:"form.reviewExp"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("发布内容经验"),t("span",[e._v("发布文章，动态，帖子时奖励，必须为整数，小于1则无经验")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入发布内容经验"},model:{value:e.form.postExp,callback:function(t){e.$set(e.form,"postExp",t)},expression:"form.postExp"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("违规扣除经验"),t("span",[e._v("必须为整数，小于1则不扣除")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入违规扣除经验"},model:{value:e.form.violationExp,callback:function(t){e.$set(e.form,"violationExp",t)},expression:"form.violationExp"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("删除扣除经验"),t("span",[e._v("删除文章，评论，动态，帖子时扣除，必须为整数，小于1则不扣除")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入删除扣除经验"},model:{value:e.form.deleteExp,callback:function(t){e.$set(e.form,"deleteExp",t)},expression:"form.deleteExp"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("发布动态最低要求经验")]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入发布动态最低要求经验"},model:{value:e.form.spaceMinExp,callback:function(t){e.$set(e.form,"spaceMinExp",t)},expression:"form.spaceMinExp"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("聊天最低要求经验值")]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入聊天最低要求经验值"},model:{value:e.form.chatMinExp,callback:function(t){e.$set(e.form,"chatMinExp",t)},expression:"form.chatMinExp"}})],1),e._v(" "),t("el-form-item",[t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.save()}}},[e._v("保存设置")])],1)],1)],1)])])],1)],1)}),[],!1,null,null,null);t.default=component.exports}}]);