"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[6672],{3905:(e,t,n)=>{n.d(t,{Zo:()=>c,kt:()=>m});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var u=r.createContext({}),l=function(e){var t=r.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},c=function(e){var t=l(e.components);return r.createElement(u.Provider,{value:t},e.children)},p="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},f=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,i=e.originalType,u=e.parentName,c=s(e,["components","mdxType","originalType","parentName"]),p=l(n),f=o,m=p["".concat(u,".").concat(f)]||p[f]||d[f]||i;return n?r.createElement(m,a(a({ref:t},c),{},{components:n})):r.createElement(m,a({ref:t},c))}));function m(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var i=n.length,a=new Array(i);a[0]=f;var s={};for(var u in t)hasOwnProperty.call(t,u)&&(s[u]=t[u]);s.originalType=e,s[p]="string"==typeof e?e:o,a[1]=s;for(var l=2;l<i;l++)a[l]=n[l];return r.createElement.apply(null,a)}return r.createElement.apply(null,n)}f.displayName="MDXCreateElement"},6525:(e,t,n)=>{n.r(t),n.d(t,{contentTitle:()=>a,default:()=>p,frontMatter:()=>i,metadata:()=>s,toc:()=>u});var r=n(7462),o=(n(7294),n(3905));const i={},a="Use UDF",s={unversionedId:"guides/use_udf",id:"guides/use_udf",isDocsHomePage:!1,title:"Use UDF",description:"Explore Flink Supported Functions",source:"@site/docs/guides/use_udf.md",sourceDirName:"guides",slug:"/guides/use_udf",permalink:"/dagger/docs/guides/use_udf",editUrl:"https://github.com/goto/dagger/edit/master/docs/docs/guides/use_udf.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Use Transformer",permalink:"/dagger/docs/guides/use_transformer"},next:{title:"Deployment",permalink:"/dagger/docs/guides/deployment"}},u=[{value:"Explore Flink Supported Functions",id:"explore-flink-supported-functions",children:[]},{value:"Explore Custom UDFs",id:"explore-custom-udfs",children:[]},{value:"Python Environment Setup",id:"python-environment-setup",children:[]}],l={toc:u},c="wrapper";function p(e){let{components:t,...n}=e;return(0,o.kt)(c,(0,r.Z)({},l,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"use-udf"},"Use UDF"),(0,o.kt)("h2",{id:"explore-flink-supported-functions"},"Explore Flink Supported Functions"),(0,o.kt)("p",null,"Queries in Dagger are similar to standard ANSI SQL with some additional syntax. So many standard SQL supported functions are also supported by Flink hence available to dagger out of the box."),(0,o.kt)("p",null,"To check if your desired function is supported by Flink follow these steps :"),(0,o.kt)("ul",null,(0,o.kt)("li",{parentName:"ul"},"Dagger uses Apache Calcite for Query evaluation. You can use Calcite supported functions in Dagger with the exceptions of some. So first check the calcite supported functions ",(0,o.kt)("a",{parentName:"li",href:"https://calcite.apache.org/docs/reference.html"},"here"),". Try to use them in a Dagger query to check if Dagger supports them."),(0,o.kt)("li",{parentName:"ul"},"Flink also supports some generic functions as Built-in Functions. You can check them out ",(0,o.kt)("a",{parentName:"li",href:"https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/functions/systemfunctions/"},"flink-udfs"),". You can use them directly."),(0,o.kt)("li",{parentName:"ul"},"If Calcite and Flink do not support your desired function, try exploring generic pre-existing custom User Defined Functions (UDFs) developed by us which are listed in the next section.")),(0,o.kt)("h2",{id:"explore-custom-udfs"},"Explore Custom UDFs"),(0,o.kt)("p",null,"Some of the use-cases can not be solved using Flink SQL & the Apache Calcite functions. In such a scenario, Dagger can be extended to meet the requirements using User Defined Functions (UDFs). UDFs can be broadly classified into the following categories:"),(0,o.kt)("ul",null,(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("h3",{parentName:"li",id:"scalar-functions"},"Scalar Functions"),(0,o.kt)("p",{parentName:"li"},"Maps zero or more values to a new value. These functions are invoked for each data in the stream.")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("h3",{parentName:"li",id:"aggregate-functions"},"Aggregate Functions"),(0,o.kt)("p",{parentName:"li"},"Aggregates one or more rows, each with one or more columns to a value. Aggregates data per dimension. int DistinctCount(int metric) // calculates distinct count of a metric in a given window. Eg: DistinctCount(driver_id) will return unique driver IDs in a window.")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("h3",{parentName:"li",id:"table-functions"},"Table Functions"),(0,o.kt)("p",{parentName:"li"},"Maps zero or more values to multiple rows and each row may have multiple columns."))),(0,o.kt)("p",null,"All the supported java udfs present in the ",(0,o.kt)("inlineCode",{parentName:"p"},"dagger-functions")," subproject in ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/goto/dagger/tree/main/dagger-functions/src/main/java/com/gotocompany/dagger/functions/udfs"},"this")," directory."),(0,o.kt)("p",null,"All the supported python udfs present in the ",(0,o.kt)("a",{parentName:"p",href:"https://github.com/goto/dagger/tree/main/dagger-py-functions/udfs/"},"dagger-py-functions")," directory."),(0,o.kt)("p",null,"Follow ",(0,o.kt)("a",{parentName:"p",href:"/dagger/docs/reference/udfs"},"this")," to find more details about the already supported UDFs in the dagger."),(0,o.kt)("p",null,"If any of the predefined functions do not meet your requirement you can create your custom UDFs by extending some implementation. Follow ",(0,o.kt)("a",{parentName:"p",href:"/dagger/docs/contribute/add_udf"},"this")," to add your custom UDFs in the dagger."),(0,o.kt)("h2",{id:"python-environment-setup"},"Python Environment Setup"),(0,o.kt)("p",null,"Python UDF execution requires Python version (3.6, 3.7 or 3.8) with PyFlink installed."),(0,o.kt)("p",null,"PyFlink is available in PyPi and can be installed as follows:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"$ python -m pip install apache-flink==1.14.3\n")),(0,o.kt)("p",null,"To satisfy the PyFlink requirement regarding the Python environment version, you need to soft link python to point to your python3 interpreter:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"ln -s /usr/bin/python3 python\n")))}p.isMDXComponent=!0}}]);