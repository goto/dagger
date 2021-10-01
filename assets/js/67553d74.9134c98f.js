"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[5034],{3905:function(e,r,t){t.d(r,{Zo:function(){return p},kt:function(){return f}});var o=t(7294);function n(e,r,t){return r in e?Object.defineProperty(e,r,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[r]=t,e}function a(e,r){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);r&&(o=o.filter((function(r){return Object.getOwnPropertyDescriptor(e,r).enumerable}))),t.push.apply(t,o)}return t}function c(e){for(var r=1;r<arguments.length;r++){var t=null!=arguments[r]?arguments[r]:{};r%2?a(Object(t),!0).forEach((function(r){n(e,r,t[r])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):a(Object(t)).forEach((function(r){Object.defineProperty(e,r,Object.getOwnPropertyDescriptor(t,r))}))}return e}function i(e,r){if(null==e)return{};var t,o,n=function(e,r){if(null==e)return{};var t,o,n={},a=Object.keys(e);for(o=0;o<a.length;o++)t=a[o],r.indexOf(t)>=0||(n[t]=e[t]);return n}(e,r);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(o=0;o<a.length;o++)t=a[o],r.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(n[t]=e[t])}return n}var s=o.createContext({}),l=function(e){var r=o.useContext(s),t=r;return e&&(t="function"==typeof e?e(r):c(c({},r),e)),t},p=function(e){var r=l(e.components);return o.createElement(s.Provider,{value:r},e.children)},d={inlineCode:"code",wrapper:function(e){var r=e.children;return o.createElement(o.Fragment,{},r)}},u=o.forwardRef((function(e,r){var t=e.components,n=e.mdxType,a=e.originalType,s=e.parentName,p=i(e,["components","mdxType","originalType","parentName"]),u=l(t),f=n,g=u["".concat(s,".").concat(f)]||u[f]||d[f]||a;return t?o.createElement(g,c(c({ref:r},p),{},{components:t})):o.createElement(g,c({ref:r},p))}));function f(e,r){var t=arguments,n=r&&r.mdxType;if("string"==typeof e||n){var a=t.length,c=new Array(a);c[0]=u;var i={};for(var s in r)hasOwnProperty.call(r,s)&&(i[s]=r[s]);i.originalType=e,i.mdxType="string"==typeof e?e:n,c[1]=i;for(var l=2;l<a;l++)c[l]=t[l];return o.createElement.apply(null,c)}return o.createElement.apply(null,t)}u.displayName="MDXCreateElement"},2530:function(e,r,t){t.r(r),t.d(r,{frontMatter:function(){return i},contentTitle:function(){return s},metadata:function(){return l},toc:function(){return p},default:function(){return u}});var o=t(7462),n=t(3366),a=(t(7294),t(3905)),c=["components"],i={},s="Overview",l={unversionedId:"advance/overview",id:"advance/overview",isDocsHomePage:!1,title:"Overview",description:"The following section covers advance features of Dagger.",source:"@site/docs/advance/overview.md",sourceDirName:"advance",slug:"/advance/overview",permalink:"/dagger/docs/advance/overview",editUrl:"https://github.com/odpf/dagger/edit/master/docs/docs/advance/overview.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Architecture",permalink:"/dagger/docs/concepts/architecture"},next:{title:"Pre Processors",permalink:"/dagger/docs/advance/pre_processor"}},p=[{value:"Pre Processor",id:"pre-processor",children:[]},{value:"Post Processor",id:"post-processor",children:[]},{value:"Longbow",id:"longbow",children:[]},{value:"Longbow+",id:"longbow-1",children:[]},{value:"DARTS",id:"darts",children:[]}],d={toc:p};function u(e){var r=e.components,t=(0,n.Z)(e,c);return(0,a.kt)("wrapper",(0,o.Z)({},d,t,{components:r,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"overview"},"Overview"),(0,a.kt)("p",null,"The following section covers advance features of Dagger."),(0,a.kt)("h3",{id:"pre-processor"},(0,a.kt)("a",{parentName:"h3",href:"/dagger/docs/advance/pre_processor"},"Pre Processor")),(0,a.kt)("p",null,"Pre processors enable you to add Flink operators/transformations before passing on the stream to the SQL query."),(0,a.kt)("h3",{id:"post-processor"},(0,a.kt)("a",{parentName:"h3",href:"/dagger/docs/advance/post_processor"},"Post Processor")),(0,a.kt)("p",null,"Post Processors give the capability to do custom stream processing after the SQL processing is performed. Complex transformation, enrichment & aggregation use cases are difficult to execute & maintain using SQL. Post Processors solve this problem through code and/or configuration."),(0,a.kt)("h3",{id:"longbow"},(0,a.kt)("a",{parentName:"h3",href:"/dagger/docs/advance/longbow"},"Longbow")),(0,a.kt)("p",null,"Longbow enables you to perform large windowed aggregation. It uses ",(0,a.kt)("a",{parentName:"p",href:"https://cloud.google.com/bigtable"},"Bigtable")," for managing historical data and SQL based DSL for configuration."),(0,a.kt)("h3",{id:"longbow-1"},(0,a.kt)("a",{parentName:"h3",href:"/dagger/docs/advance/longbow_plus"},"Longbow+")),(0,a.kt)("p",null,"Longbow+ is an enhanced version of longbow. It has additional support for complex data types for long windowed aggregations."),(0,a.kt)("h3",{id:"darts"},(0,a.kt)("a",{parentName:"h3",href:"/dagger/docs/advance/DARTS"},"DARTS")),(0,a.kt)("p",null,"DARTS allows you to join streaming data from a reference data store. It supports reference data store in the form of a list or <key, value> map. It enables the refer-table with the help of UDFs which can be used in the SQL query. Currently we only support GCS as reference data source."))}u.isMDXComponent=!0}}]);