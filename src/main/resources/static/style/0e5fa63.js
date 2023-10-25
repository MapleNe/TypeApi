(window.webpackJsonp=window.webpackJsonp||[]).push([[29],{465:function(e,t,l){"use strict";l.r(t);l(35);var o={layout:"layout",head:function(){return{title:"VIP套餐"}},data:function(){return{key:"",vipPackage:[],curPackage:{id:0,price:"",day:"",giftDay:"",intro:""},dialogAdd:!1,dialogEdit:!1,form:{name:"",price:"",day:"",giftDay:"",intro:""}}},beforeDestroy:function(){},created:function(){},mounted:function(){var e=this;localStorage.getItem("webkey")?e.key=localStorage.getItem("webkey"):(e.$message({message:"认证失效！",type:"error"}),e.$router.push({path:"/"})),e.getList()},methods:{goEdit:function(data){this.dialogEdit=!0,this.curPackage=data},add:function(){var e=this,t=e.form,data={webkey:e.key,params:JSON.stringify(t)},l=this.$loading({lock:!0,text:"Loading",spinner:"el-icon-loading",background:"rgba(0, 0, 0, 0.7)"});e.$axios.$post(e.$api.addVipType(),this.qs.stringify(data)).then((function(t){l.close(),1==t.code?(e.dialogAdd=!1,e.$message({message:t.msg,type:"success"}),e.getList()):e.$message({message:t.msg,type:"error"})})).catch((function(t){l.close(),console.log(t),e.$message({message:"接口请求异常，请检查网络！",type:"error"})}))},edit:function(){var e=this,t=e.curPackage,data={webkey:e.key,params:JSON.stringify(t)},l=this.$loading({lock:!0,text:"Loading",spinner:"el-icon-loading",background:"rgba(0, 0, 0, 0.7)"});e.$axios.$post(e.$api.updateVipType(),this.qs.stringify(data)).then((function(t){l.close(),1==t.code?(e.dialogEdit=!1,e.$message({message:t.msg,type:"success"}),e.getList()):e.$message({message:t.msg,type:"error"})})).catch((function(t){l.close(),console.log(t),e.$message({message:"接口请求异常，请检查网络！",type:"error"})}))},remove:function(e){var t=this,l=this;l.$confirm("确认删除该套餐？","操作提示",{confirmButtonText:"确认",cancelButtonText:"取消",type:"warning"}).then((function(){var data={webkey:l.key,id:e},o=t.$loading({lock:!0,text:"Loading",spinner:"el-icon-loading",background:"rgba(0, 0, 0, 0.7)"});l.$axios.$post(l.$api.deleteVipType(),t.qs.stringify(data)).then((function(e){o.close(),1==e.code?(l.$message({message:e.msg,type:"success"}),l.getList()):l.$message({message:e.msg,type:"error"})})).catch((function(e){o.close(),console.log(e),l.$message({message:"接口请求异常，请检查网络！",type:"error"})}))})).catch((function(){}))},getList:function(){var e=this,data={webkey:e.key};e.$axios.$post(e.$api.vipTypeList(),this.qs.stringify(data)).then((function(t){1==t.code&&(e.vipPackage=t.data)})).catch((function(t){console.log(t),e.$message({message:"接口请求异常，请检查网络！",type:"error"})}))}}},r=l(26),component=Object(r.a)(o,(function(){var e=this,t=e._self._c;return t("div",{staticClass:"page-container"},[t("el-row",{attrs:{gutter:15}},[t("el-col",{attrs:{span:24}},[t("div",{staticClass:"data-box"},[t("div",{staticClass:"page-title"},[t("h4",[e._v("VIP套餐")]),e._v(" "),t("p",[e._v("在这里对VIP套餐进行管理，目前有两种VIP购买模式，此类为套餐形式购买。")])]),e._v(" "),t("div",{staticClass:"page-concent"},[t("div",{staticClass:"page-operate"},[t("el-button",{attrs:{type:"success"},on:{click:function(t){e.dialogAdd=!0}}},[e._v("添加套餐")])],1),e._v(" "),t("div",{staticClass:"page-list"},[t("el-table",{staticStyle:{width:"100%"},attrs:{data:e.vipPackage,border:""}},[t("el-table-column",{attrs:{prop:"name",label:"套餐名称"}}),e._v(" "),t("el-table-column",{attrs:{prop:"price",label:"套餐价格(积分)",width:"180"}}),e._v(" "),t("el-table-column",{attrs:{prop:"day",label:"天数"}}),e._v(" "),t("el-table-column",{attrs:{prop:"giftDay",label:"奖励天数"}}),e._v(" "),t("el-table-column",{attrs:{label:"操作",width:"200"},scopedSlots:e._u([{key:"default",fn:function(l){return[t("el-button",{attrs:{type:"primary",size:"small"},on:{click:function(t){return e.goEdit(l.row)}}},[e._v("编辑")]),e._v(" "),t("el-button",{attrs:{type:"danger",size:"small"},on:{click:function(t){return e.remove(l.row.id)}}},[e._v("删除")])]}}])})],1)],1)])])])],1),e._v(" "),t("el-dialog",{attrs:{title:"添加套餐",visible:e.dialogAdd,modal:!1,width:"400px"},on:{"update:visible":function(t){e.dialogAdd=t}}},[t("div",{staticClass:"dialog-form"},[t("el-form",{ref:"form",attrs:{model:e.form,"label-position":"top","label-width":"80px"}},[t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("套餐名称")]),e._v(" "),t("el-input",{attrs:{type:"text",placeholder:"请输入套餐名称"},model:{value:e.form.name,callback:function(t){e.$set(e.form,"name",t)},expression:"form.name"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("套餐价格"),t("span",[e._v("必须为正整数")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入套餐价格"},model:{value:e.form.price,callback:function(t){e.$set(e.form,"price",t)},expression:"form.price"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("天数"),t("span",[e._v("用户将获得多少天VIP")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入天数"},model:{value:e.form.day,callback:function(t){e.$set(e.form,"day",t)},expression:"form.day"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("奖励天数"),t("span",[e._v("设置为0则不奖励，额外奖励多少天VIP")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入奖励天数"},model:{value:e.form.giftDay,callback:function(t){e.$set(e.form,"giftDay",t)},expression:"form.giftDay"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("套餐简介")]),e._v(" "),t("el-input",{attrs:{type:"textarea",placeholder:"请输入套餐简介"},model:{value:e.form.intro,callback:function(t){e.$set(e.form,"intro",t)},expression:"form.intro"}})],1)],1)],1),e._v(" "),t("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[t("el-button",{on:{click:function(t){e.dialogAdd=!1}}},[e._v("取 消")]),e._v(" "),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.add()}}},[e._v("确认添加")])],1)]),e._v(" "),t("el-dialog",{attrs:{title:"编辑套餐",visible:e.dialogEdit,modal:!1,width:"400px"},on:{"update:visible":function(t){e.dialogEdit=t}}},[t("div",{staticClass:"dialog-form"},[t("el-form",{ref:"form",attrs:{model:e.curPackage,"label-position":"top","label-width":"80px"}},[t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("套餐ID")]),e._v(" "),t("el-input",{attrs:{value:e.curPackage.id,type:"text",disabled:""}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("套餐名称")]),e._v(" "),t("el-input",{attrs:{type:"text",placeholder:"请输入套餐名称"},model:{value:e.curPackage.name,callback:function(t){e.$set(e.curPackage,"name",t)},expression:"curPackage.name"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("套餐价格"),t("span",[e._v("必须为正整数")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入套餐价格"},model:{value:e.curPackage.price,callback:function(t){e.$set(e.curPackage,"price",t)},expression:"curPackage.price"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("天数"),t("span",[e._v("用户将获得多少天VIP")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入天数"},model:{value:e.curPackage.day,callback:function(t){e.$set(e.curPackage,"day",t)},expression:"curPackage.day"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("奖励天数"),t("span",[e._v("设置为0则不奖励，额外奖励多少天VIP")])]),e._v(" "),t("el-input",{attrs:{type:"number",placeholder:"请输入奖励天数"},model:{value:e.curPackage.giftDay,callback:function(t){e.$set(e.curPackage,"giftDay",t)},expression:"curPackage.giftDay"}})],1),e._v(" "),t("el-form-item",[t("p",{staticClass:"form-label",attrs:{slot:"label"},slot:"label"},[e._v("套餐简介")]),e._v(" "),t("el-input",{attrs:{type:"textarea",placeholder:"请输入套餐简介"},model:{value:e.curPackage.intro,callback:function(t){e.$set(e.curPackage,"intro",t)},expression:"curPackage.intro"}})],1)],1)],1),e._v(" "),t("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[t("el-button",{on:{click:function(t){e.dialogEdit=!1}}},[e._v("取 消")]),e._v(" "),t("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.edit()}}},[e._v("保存修改")])],1)])],1)}),[],!1,null,null,null);t.default=component.exports}}]);