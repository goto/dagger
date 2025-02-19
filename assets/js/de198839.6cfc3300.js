"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[6080],{2798:(e,t,r)=>{r.r(t),r.d(t,{contentTitle:()=>s,default:()=>u,frontMatter:()=>a,metadata:()=>i,toc:()=>c});var n=r(8168),o=(r(6540),r(5680));const a={},s="Lifecycle",i={unversionedId:"concepts/lifecycle",id:"concepts/lifecycle",isDocsHomePage:!1,title:"Lifecycle",description:"Architecturally after the creation of Dagger, it goes through several stages before materializing the results to an output stream.",source:"@site/docs/concepts/lifecycle.md",sourceDirName:"concepts",slug:"/concepts/lifecycle",permalink:"/dagger/docs/concepts/lifecycle",editUrl:"https://github.com/goto/dagger/edit/master/docs/docs/concepts/lifecycle.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Basics",permalink:"/dagger/docs/concepts/basics"},next:{title:"Architecture",permalink:"/dagger/docs/concepts/architecture"}},c=[],l={toc:c},p="wrapper";function u(e){let{components:t,...a}=e;return(0,o.yg)(p,(0,n.A)({},l,a,{components:t,mdxType:"MDXLayout"}),(0,o.yg)("h1",{id:"lifecycle"},"Lifecycle"),(0,o.yg)("p",null,"Architecturally after the creation of Dagger, it goes through several stages before materializing the results to an output stream."),(0,o.yg)("p",null,(0,o.yg)("img",{src:r(3798).A})),(0,o.yg)("ul",null,(0,o.yg)("li",{parentName:"ul"},(0,o.yg)("inlineCode",{parentName:"li"},"Stage-1")," : Dagger registers all defined configurations. JobManager validates the configurations and the query and creates a job-graph for the same."),(0,o.yg)("li",{parentName:"ul"},(0,o.yg)("inlineCode",{parentName:"li"},"Stage-2")," : Data consumption from the source and its deserialization as per the protobuf schema"),(0,o.yg)("li",{parentName:"ul"},(0,o.yg)("inlineCode",{parentName:"li"},"Stage-3")," : Before executing the streaming SQL, Dagger undergoes a Pre-processor stage. Pre-processor is similar to post processors and currently support only transformers. They can be used to do some filtration of data or to do some basic processing before SQL."),(0,o.yg)("li",{parentName:"ul"},(0,o.yg)("inlineCode",{parentName:"li"},"Stage-4")," : In this stage, Dagger has to register custom UDFs. After that, it executes the SQL on the input stream and produces a resultant Output stream. But in the case of complex business requirements, SQL is not just enough to handle the complexity of the problem. So there comes the Post Processors."),(0,o.yg)("li",{parentName:"ul"},(0,o.yg)("inlineCode",{parentName:"li"},"Stage-5")," : In the fourth stage or Post Processors stage, the output of the previous stage is taken as input and some complex transformation logic can be applied on top of it to produce the final result. There can be three types of Post Processors: external, internal, and transformers. You can find more on post processors and their responsibilities here."),(0,o.yg)("li",{parentName:"ul"},(0,o.yg)("inlineCode",{parentName:"li"},"Stage-6")," : In the final stage or sink stage, the final output is serialized accordingly and sent to a downstream (which can either be Kafka, BigQuery or InfluxDB) for further use cases.")))}u.isMDXComponent=!0},3798:(e,t,r)=>{r.d(t,{A:()=>n});const n=r.p+"assets/images/dagger_lifecycle-c0a6441f0ec4e000c21794cae9784efc.png"},5680:(e,t,r)=>{r.d(t,{xA:()=>p,yg:()=>m});var n=r(6540);function o(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function a(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function s(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?a(Object(r),!0).forEach((function(t){o(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):a(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function i(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},a=Object.keys(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var c=n.createContext({}),l=function(e){var t=n.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):s(s({},t),e)),r},p=function(e){var t=l(e.components);return n.createElement(c.Provider,{value:t},e.children)},u="mdxType",g={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},f=n.forwardRef((function(e,t){var r=e.components,o=e.mdxType,a=e.originalType,c=e.parentName,p=i(e,["components","mdxType","originalType","parentName"]),u=l(r),f=o,m=u["".concat(c,".").concat(f)]||u[f]||g[f]||a;return r?n.createElement(m,s(s({ref:t},p),{},{components:r})):n.createElement(m,s({ref:t},p))}));function m(e,t){var r=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var a=r.length,s=new Array(a);s[0]=f;var i={};for(var c in t)hasOwnProperty.call(t,c)&&(i[c]=t[c]);i.originalType=e,i[u]="string"==typeof e?e:o,s[1]=i;for(var l=2;l<a;l++)s[l]=r[l];return n.createElement.apply(null,s)}return n.createElement.apply(null,r)}f.displayName="MDXCreateElement"}}]);