"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[9083],{471:(e,r,a)=>{a.r(r),a.d(r,{contentTitle:()=>s,default:()=>l,frontMatter:()=>o,metadata:()=>i,toc:()=>m});var t=a(8168),n=(a(6540),a(5680));const o={},s="ADD Transformer",i={unversionedId:"contribute/add_transformer",id:"contribute/add_transformer",isDocsHomePage:!1,title:"ADD Transformer",description:"The Transformers give Dagger the capability for applying any custom transformation logic user wants on Data(pre or post aggregated). In simple terms, Transformers are user-defined Java code that can do transformations like Map, Filter, Flat maps etc. Find more information on Transformers here.",source:"@site/docs/contribute/add_transformer.md",sourceDirName:"contribute",slug:"/contribute/add_transformer",permalink:"/dagger/docs/contribute/add_transformer",editUrl:"https://github.com/goto/dagger/edit/master/docs/docs/contribute/add_transformer.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Development Guide",permalink:"/dagger/docs/contribute/development"},next:{title:"ADD UDF",permalink:"/dagger/docs/contribute/add_udf"}},m=[],c={toc:m},p="wrapper";function l(e){let{components:r,...a}=e;return(0,n.yg)(p,(0,t.A)({},c,a,{components:r,mdxType:"MDXLayout"}),(0,n.yg)("h1",{id:"add-transformer"},"ADD Transformer"),(0,n.yg)("p",null,"The Transformers give Dagger the capability for applying any custom ",(0,n.yg)("em",{parentName:"p"},"transformation logic")," user wants on Data(pre or post aggregated). In simple terms, Transformers are user-defined Java code that can do transformations like Map, Filter, Flat maps etc. Find more information on Transformers here."),(0,n.yg)("p",null,"Many Transformation logics are pre-supported in Dagger. But since Transformers are more advanced ways of injecting business logic as plugins to Dagger, there can be cases when existing Transformers are not sufficient for user requirement. This section documents how you can add Transformers to dagger."),(0,n.yg)("p",null,"For adding custom Transformers follow these steps"),(0,n.yg)("ul",null,(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"Ensure none of the ",(0,n.yg)("a",{parentName:"p",href:"/dagger/docs/reference/transformers"},"built-in Transformers")," suits your requirement.")),(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"Transformers take ",(0,n.yg)("a",{parentName:"p",href:"https://github.com/goto/dagger/blob/main/dagger-common/src/main/java/com/gotocompany/dagger/common/core/StreamInfo.java"},"StreamInfo")," which is a wrapper around Flink DataStream as input and transform them to some other StreamInfo/DataStream.")),(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"To define a new Transformer implement Transformer interface. The contract of Transformers is defined ",(0,n.yg)("a",{parentName:"p",href:"https://github.com/goto/dagger/blob/main/dagger-common/src/main/java/com/gotocompany/dagger/common/core/Transformer.java"},"here"),".")),(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"Since an input DataStream is available in Transformer, all the Flink supported operators which transform ",(0,n.yg)("inlineCode",{parentName:"p"},"DataStream -> DataStream")," can be applied/used by default for the transformations. Operators are how Flink exposes classic Map-reduce type functionalities. Read more about Flink Operators ",(0,n.yg)("a",{parentName:"p",href:"https://ci.apache.org/projects/flink/flink-docs-release-1.14/dev/stream/operators/"},"here"),".")),(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"In the case of single Operator Transformation you can extend the desired Operator in the Transformer class itself. For example, follow this code of ",(0,n.yg)("a",{parentName:"p",href:"https://github.com/goto/dagger/blob/main/dagger-functions/src/main/java/com/gotocompany/dagger/functions/transformers/HashTransformer.java"},"HashTransformer"),". You can also define multiple chaining operators to Transform Data.")),(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"A configuration ",(0,n.yg)("inlineCode",{parentName:"p"},"transformation_arguments")," inject the required parameters as a Constructor argument to the Transformer class. From the config point of view, these are simple Map of String and Object. So you need to cast them to your desired data types. Find a more detailed overview of the transformer example ",(0,n.yg)("a",{parentName:"p",href:"/dagger/docs/guides/use_transformer"},"here"),".")),(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"Transformers are injected into the ",(0,n.yg)("inlineCode",{parentName:"p"},"dagger-core")," during runtime using Java reflection. So unlike UDFs, they don't need registration. You just need to mention the fully qualified Transformer Java class Name in the configurations.")),(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"Bump up the version and raise a PR for the Transformer. Also please add the Transformer to the ",(0,n.yg)("a",{parentName:"p",href:"/dagger/docs/reference/transformers"},"list of Transformers doc"),". Once the PR gets merged the transformer should be available to be used.")),(0,n.yg)("li",{parentName:"ul"},(0,n.yg)("p",{parentName:"li"},"If you have specific use-cases you are solving using Transformers and you don't want to add them to the open-source repo, you can have a separate local codebase for those Transformers and add it to the classpath of the dagger. With the correct Transformation configurations, they should be available to use out of the box."))),(0,n.yg)("p",null,(0,n.yg)("inlineCode",{parentName:"p"},"Note"),": ",(0,n.yg)("em",{parentName:"p"},"Please go through the ",(0,n.yg)("a",{parentName:"em",href:"/dagger/docs/contribute/contribution"},"Contribution guide")," to know about all the conventions and practices we tend to follow and to know about the contribution process to the dagger.")))}l.isMDXComponent=!0},5680:(e,r,a)=>{a.d(r,{xA:()=>p,yg:()=>f});var t=a(6540);function n(e,r,a){return r in e?Object.defineProperty(e,r,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[r]=a,e}function o(e,r){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var t=Object.getOwnPropertySymbols(e);r&&(t=t.filter((function(r){return Object.getOwnPropertyDescriptor(e,r).enumerable}))),a.push.apply(a,t)}return a}function s(e){for(var r=1;r<arguments.length;r++){var a=null!=arguments[r]?arguments[r]:{};r%2?o(Object(a),!0).forEach((function(r){n(e,r,a[r])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):o(Object(a)).forEach((function(r){Object.defineProperty(e,r,Object.getOwnPropertyDescriptor(a,r))}))}return e}function i(e,r){if(null==e)return{};var a,t,n=function(e,r){if(null==e)return{};var a,t,n={},o=Object.keys(e);for(t=0;t<o.length;t++)a=o[t],r.indexOf(a)>=0||(n[a]=e[a]);return n}(e,r);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(t=0;t<o.length;t++)a=o[t],r.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(n[a]=e[a])}return n}var m=t.createContext({}),c=function(e){var r=t.useContext(m),a=r;return e&&(a="function"==typeof e?e(r):s(s({},r),e)),a},p=function(e){var r=c(e.components);return t.createElement(m.Provider,{value:r},e.children)},l="mdxType",g={inlineCode:"code",wrapper:function(e){var r=e.children;return t.createElement(t.Fragment,{},r)}},d=t.forwardRef((function(e,r){var a=e.components,n=e.mdxType,o=e.originalType,m=e.parentName,p=i(e,["components","mdxType","originalType","parentName"]),l=c(a),d=n,f=l["".concat(m,".").concat(d)]||l[d]||g[d]||o;return a?t.createElement(f,s(s({ref:r},p),{},{components:a})):t.createElement(f,s({ref:r},p))}));function f(e,r){var a=arguments,n=r&&r.mdxType;if("string"==typeof e||n){var o=a.length,s=new Array(o);s[0]=d;var i={};for(var m in r)hasOwnProperty.call(r,m)&&(i[m]=r[m]);i.originalType=e,i[l]="string"==typeof e?e:n,s[1]=i;for(var c=2;c<o;c++)s[c]=a[c];return t.createElement.apply(null,s)}return t.createElement.apply(null,a)}d.displayName="MDXCreateElement"}}]);