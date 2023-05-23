"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[2520],{3905:(e,t,r)=>{r.d(t,{Zo:()=>p,kt:()=>g});var a=r(7294);function n(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function o(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,a)}return r}function i(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?o(Object(r),!0).forEach((function(t){n(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):o(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function l(e,t){if(null==e)return{};var r,a,n=function(e,t){if(null==e)return{};var r,a,n={},o=Object.keys(e);for(a=0;a<o.length;a++)r=o[a],t.indexOf(r)>=0||(n[r]=e[r]);return n}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(a=0;a<o.length;a++)r=o[a],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(n[r]=e[r])}return n}var s=a.createContext({}),c=function(e){var t=a.useContext(s),r=t;return e&&(r="function"==typeof e?e(t):i(i({},t),e)),r},p=function(e){var t=c(e.components);return a.createElement(s.Provider,{value:t},e.children)},u="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},m=a.forwardRef((function(e,t){var r=e.components,n=e.mdxType,o=e.originalType,s=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),u=c(r),m=n,g=u["".concat(s,".").concat(m)]||u[m]||d[m]||o;return r?a.createElement(g,i(i({ref:t},p),{},{components:r})):a.createElement(g,i({ref:t},p))}));function g(e,t){var r=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var o=r.length,i=new Array(o);i[0]=m;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l[u]="string"==typeof e?e:n,i[1]=l;for(var c=2;c<o;c++)i[c]=r[c];return a.createElement.apply(null,i)}return a.createElement.apply(null,r)}m.displayName="MDXCreateElement"},266:(e,t,r)=>{r.r(t),r.d(t,{contentTitle:()=>i,default:()=>u,frontMatter:()=>o,metadata:()=>l,toc:()=>s});var a=r(7462),n=(r(7294),r(3905));const o={},i="Distance computation using Java UDF",l={unversionedId:"examples/distance_java_udf",id:"examples/distance_java_udf",isDocsHomePage:!1,title:"Distance computation using Java UDF",description:"About this example",source:"@site/docs/examples/distance_java_udf.md",sourceDirName:"examples",slug:"/examples/distance_java_udf",permalink:"/dagger/docs/examples/distance_java_udf",editUrl:"https://github.com/goto/dagger/edit/master/docs/docs/examples/distance_java_udf.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Removing duplicate records using Transformers",permalink:"/dagger/docs/examples/deduplication_transformer"},next:{title:"Stream enrichment using ElasticSearch source",permalink:"/dagger/docs/examples/elasticsearch_enrichment"}},s=[{value:"About this example",id:"about-this-example",children:[]},{value:"Before Trying This Example",id:"before-trying-this-example",children:[]},{value:"Steps",id:"steps",children:[]}],c={toc:s},p="wrapper";function u(e){let{components:t,...r}=e;return(0,n.kt)(p,(0,a.Z)({},c,r,{components:t,mdxType:"MDXLayout"}),(0,n.kt)("h1",{id:"distance-computation-using-java-udf"},"Distance computation using Java UDF"),(0,n.kt)("h2",{id:"about-this-example"},"About this example"),(0,n.kt)("p",null,"In this example, we will use a User-Defined Function in Dagger to compute the distance between the driver pickup location and the driver dropoff location for each booking log (as Kafka record) . By the end of this example we will understand how to use Dagger UDFs to add more functionality and simplify our queries."),(0,n.kt)("h2",{id:"before-trying-this-example"},"Before Trying This Example"),(0,n.kt)("ol",null,(0,n.kt)("li",{parentName:"ol"},(0,n.kt)("p",{parentName:"li"},(0,n.kt)("strong",{parentName:"p"},"We must have Docker installed"),". We can follow ",(0,n.kt)("a",{parentName:"p",href:"https://docs.docker.com/get-docker/"},"this guide")," on how to install and set up Docker in your local machine.")),(0,n.kt)("li",{parentName:"ol"},(0,n.kt)("p",{parentName:"li"},"Clone Dagger repository into your local"),(0,n.kt)("pre",{parentName:"li"},(0,n.kt)("code",{parentName:"pre",className:"language-shell"},"git clone https://github.com/goto/dagger.git\n")))),(0,n.kt)("h2",{id:"steps"},"Steps"),(0,n.kt)("p",null,"Following are the steps for setting up dagger in docker compose -"),(0,n.kt)("ol",null,(0,n.kt)("li",{parentName:"ol"},"cd into the aggregation directory:",(0,n.kt)("pre",{parentName:"li"},(0,n.kt)("code",{parentName:"pre",className:"language-shell"},"cd dagger/quickstart/examples/aggregation/tumble_window \n"))),(0,n.kt)("li",{parentName:"ol"},"fire this command to spin up the docker compose:",(0,n.kt)("pre",{parentName:"li"},(0,n.kt)("code",{parentName:"pre",className:"language-shell"},"docker compose up \n")),"Hang on for a while as it installs all the required dependencies and starts all the required services. After a while we should see the output of the Dagger SQL query in the terminal, which will be the distance between the driver pickup location and the driver dropoff location for each booking log."),(0,n.kt)("li",{parentName:"ol"},"fire this command to gracefully close the docker compose:",(0,n.kt)("pre",{parentName:"li"},(0,n.kt)("code",{parentName:"pre",className:"language-shell"},"docker compose down \n")),"This will stop and remove all the containers.")),(0,n.kt)("p",null,"Congratulations, we are now able to use Dagger UDF to calculate distance easily!"))}u.isMDXComponent=!0}}]);