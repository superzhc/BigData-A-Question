---
title: Chrome插件开发
date: 2017-11-25 14:21:13
tags: [web,chrome]
---
# Chrome插件

## 初步接触Chrome扩展应用开发

### 介绍

Chrome扩展主要用于对浏览器功能的增强，它更强调与浏览器相结合。比如Chrome扩展可以在浏览器的工具栏和地址栏中显示图标，它可以更改用户当前浏览的网页中的内容，也可以更改浏览器代理服务器的设置等等。

Chrome扩展是一系列文件的集合，这些文件包括HTML文件、CSS样式文件、JavaScript脚本文件、图片等静态文件以及manifest.json。

扩展被安装后，Chrome就会读取扩展中的manifest.json文件。这个文件的文件名固定为manifest.json，内容是按照一定格式描述的扩展相关信息，如扩展名称、版本、更新地址、请求的权限、扩展的UI界面入口等等。这样Chrome就可以知道在浏览器中如何呈现这个扩展，以及这个扩展如何同用户进行交互。

Chrome扩展更像是一个运行于本地的网站，只是它可以利用Chrome平台提供的丰富的接口，获得更加全面的信息，进行更加复杂的操作。而它的界面则使用HTML和CSS进行描述，这样的好处是可以用很短的时间构建出赏心悦目的UI。界面的渲染完全不需要开发者操心，而是交给Chrome去做。同时由于JavaScript是一门解释语言1，无需对其配置编译器，调试代码时你只要刷新一下浏览器就可以看到修改后的结果，这使得开发周期大大缩短。

### 第一个Chrome扩展

首先，创建插件文件夹chromedev，在此文件夹中新建一个名为manifest.json的文件，内容如下：

```json
{
    "manifest_version": 2,
    "name": "我的时钟",
    "version": "1.0",
    "description": "我的第一个Chrome扩展",
    "icons": {
        "16": "images/icon16.png",
        "48": "images/icon48.png",
        "128": "images/icon128.png"
    },
    "browser_action": {
        "default_icon": {
            "19": "images/icon19.png",
            "38": "images/icon38.png"
        },
        "default_title": "我的时钟",
        "default_popup": "popup.html"
    }
}
```

接下来编写popup.html,将相关js、css、images放到相应位置。

将扩展载入到Chrome中运行，打开扩展程序中开发模式，点击加载扩展程序，选择到chromedev文件夹，就可以在浏览器工具栏中看到我们的扩展了。

### Manifest文件格式

Chrome扩展都包含一个Manifest文件——manifest.json，这个文件可以告诉Chrome关于这个扩展的相关信息，它是整个扩展的入口，也是Chrome扩展必不可少的部分。

Manifest文件使用JSON格式保存数据。Chrome扩展的Manifest必须包含name、version和manifest\_version属性，目前来说manifest\_version属性值只能为数字2。其他常用的可选属性还有browser\_action、page_\action、background、permissions、options\_page、content\_scripts。保存一份mainifest.json模版：

```json
{
  // 清单文件的版本，这个必须写，而且必须是2
  "manifest_version": 2,
  // 插件的名称
  "name": "My Extension",
  // 插件的版本
  "version": "versionString",

  // Recommended
  // 默认语言
  "default_locale": "en",
  // 插件描述
  "description": "A plain text description",
  // 图标，一般偷懒全部用一个尺寸的也没问题,会自动进行拉伸
  "icons": {
    "16": "images/icon16.png",
    "48": "images/icon48.png",
    "128": "images/icon128.png"
  },

  // 浏览器右上角图标设置，browser_action、page_action、app必须三选一，甚至可以不选
  "browser_action": {
    "default_icon": {
        "19": "images/icon19.png",
        "38": "images/icon38.png"
    },
    "default_title": "Extension Title",
    "default_popup": "popup.html"
  },
  //// 当某些特定页面打开才显示的图标
  "page_action": {...},

  // Optional
  "author": ...,
  "automation": ...,
  // 会一直常驻的后台JS或后台页面
  "background": {
    // Recommended
    "persistent": false
  },
  "background_page": ...,
  "chrome_settings_overrides": {...},
  "chrome_ui_overrides": {
    "bookmarks_ui": {
      "remove_bookmark_shortcut": true,
      "remove_button": true
    }
  },

  // 覆盖浏览器默认页面
  "chrome_url_overrides": {...},
  "commands": {...},
  "content_capabilities": ...,

  // 需要直接注入页面的JS
  "content_scripts": [
    {
        //"matches": ["http://*/*", "https://*/*"],
        // "<all_urls>" 表示匹配所有地址
        "matches": ["<all_urls>"],
        // 多个JS按顺序注入
        "js": ["js/jquery-1.8.3.js", "js/content-script.js"],
        // JS的注入可以随便一点，但是CSS的注意就要千万小心了，因为一不小心就可能影响全局样式
        "css": ["css/custom.css"],
        // 代码注入的时间，可选值： "document_start", "document_end", or "document_idle"，最后一个表示页面空闲时，默认document_idle
        "run_at": "document_start"
    },
    // 这里仅仅是为了演示content-script可以配置多个规则
    {
        "matches": ["*://*/*.png", "*://*/*.jpg", "*://*/*.gif", "*://*/*.bmp"],
        "js": ["js/show-image-content-size.js"]
        }
  ],
  "content_security_policy": "policyString",
  "converted_from_user_script": ...,
  "current_locale": ...,
  "declarative_net_request": ...,
  "devtools_page": "devtools.html",
  "event_rules": [{...}],
  "externally_connectable": {
    "matches": ["*://*.example.com/*"]
  },
  "file_browser_handlers": [...],
  "file_system_provider_capabilities": {
    "configurable": true,
    "multiple_mounts": true,
    "source": "network"
  },
  // 插件主页，这个很重要，不要浪费了这个免费广告位
  "homepage_url": "http://path/to/homepage",
  "import": [{"id": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}],
  "incognito": "spanning, split, or not_allowed",
  "input_components": ...,
  "key": "publicKey",
  "minimum_chrome_version": "versionString",
  "nacl_modules": [...],
  "oauth2": ...,
  "offline_enabled": true,
  "omnibox": {
    "keyword": "aString"
  },
  "optional_permissions": ["tabs"],
  "options_page": "options.html",
  "options_ui": {
    "chrome_style": true,
    "page": "options.html"
  },

  // 权限申请
  "permissions": [
    "contextMenus", // 右键菜单
    "tabs", // 标签
    "notifications", // 通知
    "webRequest", // web请求
    "webRequestBlocking",
    "storage", // 插件本地存储
    "http://*/*", // 可以通过executeScript或者insertCSS访问的网站
    "https://*/*" // 可以通过executeScript或者insertCSS访问的网站
  ],
  "platforms": ...,
  "plugins": [...],
  "requirements": {...},
  "sandbox": [...],
  "short_name": "Short Name",
  "signature": ...,
  "spellcheck": ...,
  "storage": {
    "managed_schema": "schema.json"
  },
  "system_indicator": ...,
  "tts_engine": {...},
  "update_url": "http://path/to/updateInfo.xml",
  "version_name": "aString",
  // 普通页面能够直接访问的插件资源列表，如果不设置是无法直接访问的
  "web_accessible_resources": [...]
}
```

## Chrome扩展基础

### 操作用户正在浏览的页面

通过Chrome扩展我们可以对用户当前浏览的页面进行操作，实际上就是对用户当前浏览页面的DOM进行操作。通过Manifest中的content_scripts属性可以指定将哪些脚本何时注入到哪些页面中，当用户访问这些页面后，相应脚本即可自动运行，从而对页面DOM进行操作。

简而言之，其实就是Chrome插件中向页面注入脚本的一种形式（虽然名为script，其实还可以包括css的），借助content-scripts我们可以实现通过配置的方式轻松向指定页面注入JS和CSS，最常见的比如：广告屏蔽、页面CSS定制，等等。

```json
{
    // 需要直接注入页面的JS
    "content_scripts":
    [
        {
            //"matches": ["http://*/*", "https://*/*"],
            // "<all_urls>" 表示匹配所有地址
            "matches": ["<all_urls>"],
            // 多个JS按顺序注入
            "js": ["js/jquery-1.8.3.js", "js/content-script.js"],
            // JS的注入可以随便一点，但是CSS的注意就要千万小心了，因为一不小心就可能影响全局样式
            "css": ["css/custom.css"],
            // 代码注入的时间，可选值： "document_start", "document_end", or "document_idle"，最后一个表示页面空闲时，默认document_idle
            "run_at": "document_start"
        }
    ]
}
```

Manifest的content\_scripts属性值为数组类型，数组的每个元素可以包含matches、exclude\_matches、css、js、run\_at、all\_frames、include\_globs和exclude\_globs等属性。

- matches属性定义了哪些页面会被注入脚本
- exclude_matches则定义了哪些页面不会被注入脚本
- css和js对应要注入的样式表和JavaScript
- run_at定义了何时进行注入,可选值： "document\_start", "document\_end", or "document\_idle"，最后一个表示页面空闲时，默认document\_idle
- all\_frames定义脚本是否会注入到嵌入式框架中
- include\_globs和exclude\_globs则是全局URL匹配，最终脚本是否会被注入由matches、exclude_matches、include_globs和exclude\_globs的值共同决定。简单的说，如果URL匹配mathces值的同时也匹配include\_globs的值，会被注入；如果URL匹配exclude\_matches的值或者匹配exclude\_globs的值，则不会被注入。

content\_scripts中的脚本只是共享页面的DOM，而并不共享页面内嵌JavaScript的命名空间。也就是说，如果当前页面中的JavaScript有一个全局变量a，content\_scripts中注入的脚本也可以有一个全局变量a，两者不会相互干扰。当然你也无法通过content\_scripts访问到页面本身内嵌JavaScript的变量和函数。如要访问页面JS（例如某个JS变量），只能通过injected js来实现。content\_scripts不能访问绝大部分chrome.xxx.api，除了下面这4种：

- chrome.extension(getURL , inIncognitoContext , lastError , onRequest , sendRequest)
- chrome.i18n
- chrome.runtime(connect , getManifest , getURL , id , onConnect , onMessage , sendMessage)
- chrome.storage

### 跨域请求

跨域指的是JavaScript通过XMLHttpRequest请求数据时，调用JavaScript的页面所在的域和被请求页面的域不一致。对于网站来说，浏览器出于安全考虑是不允许跨域。另外，对于域相同，但端口或协议不同时，浏览器也是禁止的。

但这个规则如果同样限制Chrome扩展应用，就会使其能力大打折扣，所以Google允许Chrome扩展应用不必受限于跨域限制。但出于安全考虑，需要在Manifest的permissions属性中声明需要跨域的权限。

### 常驻后台

在Manifest中指定background域可以使扩展常驻后台。它的生命周期是插件中所有类型页面中最长的，它随着浏览器的打开而打开，随着浏览器的关闭而关闭，所以通常把需要一直运行的、启动就运行的、全局的代码放在background里面。

background的权限非常高，几乎可以调用所有的Chrome扩展API（除了devtools），而且它可以无限制跨域，也就是可以跨域访问任何网站而无需要求对方设置CORS。

```json
{
    // 会一直常驻的后台JS或后台页面
    "background":
    {
        // 2种指定方式，如果指定JS，那么会自动生成一个背景页
        "page": "background.html"
        //"scripts": ["js/background.js"]
    }
}
```

background可以包含三种属性，分别是scripts、page和persistent。如果指定了scripts属性，则Chrome会在扩展启动时自动创建一个包含所有指定脚本的页面；如果指定了page属性，则Chrome会将指定的HTML文件作为后台页面运行。通常我们只需要使用scripts属性即可，除非在后台页面中需要构建特殊的HTML——但一般情况下后台页面的HTML我们是看不到的。persistent属性定义了常驻后台的方式——当其值为true时，表示扩展将一直在后台运行，无论其是否正在工作；当其值为false时，表示扩展在后台按需运行，这就是Chrome后来提出的Event Page。

Event Page,它的生命周期是：在被需要时加载，在空闲时被关闭，什么叫被需要时呢？比如第一次安装、插件更新、有content\_script向它发送消息，等等,它可以有效减小扩展对内存的消耗，如非必要，请将persistent设置为false。persistent的默认值为true。

**注：** 虽然你可以通过chrome-extension://xxx/background.html直接打开后台页，但是你打开的后台页和真正一直在后台运行的那个页面不是同一个，换句话说，你可以打开无数个background.html，但是真正在后台常驻的只有一个，而且这个你永远看不到它的界面，只能调试它的代码。

### 带选项页面的扩展

Chrome通过Manifest文件的options_page属性为开发者提供了这样的接口，可以为扩展指定一个选项页面。当用户在扩展图标上点击右键，选择菜单中的“选项”后，就会打开这个页面。

对于网站来说，用户的设置通常保存在Cookies中，或者保存在网站服务器的数据库中。对于JavaScript来说，一些数据可以保存在变量中，但如果用户重新启动浏览器，这些数据就会消失。那么如何在扩展中保存用户的设置呢？我们可以使用HTML5新增的localStorage接口。除了localStorage接口以外，还可以使用其他的储存方法。

localStorage是HTML5新增的方法，它允许JavaScript在用户计算机硬盘上永久储存数据（除非用户主动删除）。但localStorage也有一些限制，首先是localStorage和Cookies类似，都有域的限制，运行在不同域的JavaScript无法调用其他域localStorage的数据；其次是单个域在localStorage中存储数据的大小通常有限制（虽然W3C没有给出限制），对于Chrome这个限制是5MB；最后localStorage只能储存字符串型的数据，无法保存数组和对象，但可以通过join、toString和JSON.stringify等方法先转换成字符串再储存。localStorage除了使用localStorage.namespace的方法引用和写入数据外，还可以使用localStorage['namespace']的形式。请注意第二种方法namespace要用引号包围，单引号和双引号都可以。如果想彻底删除一个数据，可以使用localStorage.removeItem('namespace')方法。

**注：** Chrome不允许将JavaScript内嵌在HTML文件中

### 扩展页面之间的通信

#### popup和background

popup可以直接调用background中的JS方法，也可以直接访问background的DOM：

```js
// background.js
function test()
{
    alert('我是background！');
}

// popup.js
var bg = chrome.extension.getBackgroundPage();
bg.test(); // 访问bg的函数
alert(bg.document.body.innerHTML); // 访问bg的DOM
```

至于background访问popup如下（前提是popup已经打开）：

```js
var views = chrome.extension.getViews({type:'popup'});
if(views.length > 0) {
    console.log(views[0].location.href);
}
```

#### popup或者bg与content之间的通信

Chrome提供的大部分API是不支持在content\_scripts中运行的，但runtime.sendMessage和runtime.onMessage可以在content\_scripts中运行，所以扩展的其他页面也可以同content\_scripts相互通信。

runtime.sendMessage完整的方法为：`chrome.runtime.sendMessage(extensionId, message, options, callback)`

- extensionId为所发送消息的目标扩展，如果不指定这个值，则默认为发起此消息的扩展本身；
- message为要发送的内容，类型随意，内容随意，比如可以是'Hello'，也可以是{action: 'play'}、2013和['Jim', 'Tom', 'Kate']等等；
- options为对象类型，包含一个值为布尔型的includeTlsChannelId属性，此属性的值决定扩展发起此消息时是否要将TLS通道ID发送给监听此消息的外部扩展，这是有关加强用户连接安全性的技术，如果这个参数你捉摸不透，不必理睬它；
- callback是回调函数，用于接收返回结果，同样是一个可选参数

runtime.onMessage完整的方法为：`chrome.runtime.onMessage.addListener(callback)`

此处的callback为必选参数，为回调函数。callback接收到的参数有三个，分别是message、sender和sendResponse，即消息内容、消息发送者相关信息和相应函数。其中sender对象包含4个属性，分别是tab、id、url和tlsChannelId，tab是发起消息的标签

#### popup或者bg向content主动发送消息

background.js或者popup.js：

```js
function sendMessageToContentScript(message, callback)
{
    chrome.tabs.query({active: true, currentWindow: true}, function(tabs)
    {
        chrome.tabs.sendMessage(tabs[0].id, message, function(response)
        {
            if(callback) callback(response);
        });
    });
}
sendMessageToContentScript({cmd:'test', value:'你好，我是popup！'}, function(response)
{
    console.log('来自content的回复：'+response);
});
```

content_script.js接收：

```js
chrome.runtime.onMessage.addListener(function(request, sender, sendResponse)
{
    // console.log(sender.tab ?"from a content script:" + sender.tab.url :"from the extension");
    if(request.cmd == 'test') alert(request.value);
    sendResponse('我收到了你的消息！');
});
```

双方通信直接发送的都是JSON对象，不是JSON字符串，所以无需解析，很方便（当然也可以直接发送字符串）。

#### content_script主动发消息给后台

content_script.js：

```js
chrome.runtime.sendMessage({greeting: '你好，我是content-script呀，我主动发消息给后台！'}, function(response) {
    console.log('收到来自后台的回复：' + response);
});
```

background.js 或者 popup.js：

// 监听来自content-script的消息
chrome.runtime.onMessage.addListener(function(request, sender, sendResponse)
{
    console.log('收到来自content-script的消息：');
    console.log(request, sender, sendResponse);
    sendResponse('我是后台，我已收到你的消息：' + JSON.stringify(request));
});

注意事项：

- content_scripts向popup主动发消息的前提是popup必须打开！否则需要利用background作中转；
- 如果background和popup同时监听，那么它们都可以同时收到消息，但是只有一个可以sendResponse，一个先发送了，那么另外一个再发送就无效；

#### injected script和content_script

content_script和页面内的脚本（injected-script自然也属于页面内的脚本）之间唯一共享的东西就是页面的DOM元素，有2种方法可以实现二者通讯：

- 可以通过window.postMessage和window.addEventListener来实现二者消息通讯；
- 通过自定义DOM事件来实现；

### 动态注入

#### 动态注入和执行JS

在background和popup中无法直接访问页面DOM，但是可以通过chrome.tabs.executeScript来执行脚本，从而实现访问web页面的DOM。

manifest.json

```json
{
    "name": "动态JS注入演示",
    ...
    "permissions": [
        "tabs", "http://*/*", "https://*/*"
    ],
    ...
}
```

```js
// 动态执行JS代码
chrome.tabs.executeScript(tabId, {code: 'document.body.style.backgroundColor="red"'});
// 动态执行JS文件
chrome.tabs.executeScript(tabId, {file: 'some-script.js'});
```

#### 动态注入CSS

manifest.json

```json
{
    "name": "动态CSS注入演示",
    ...
    "permissions": [
        "tabs", "http://*/*", "https://*/*"
    ],
    ...
}
```

```js
// 动态执行CSS代码
chrome.tabs.insertCSS(tabId, {code: 'xxx'});
// 动态执行CSS文件
chrome.tabs.insertCSS(tabId, {file: 'some-style.css'});
```

### 储存数据

一个程序免不了要储存数据，对于Chrome扩展也是这样。通常Chrome扩展使用以下三种方法中的一种来储存数据：第一种是使用HTML5的localStorage，第二种是使用Chrome提供的存储API；第三种是使用Web SQL Database。

对于一般的扩展，“设置”这种简单的数据可以优先选择第一种，因为这种方法使用简单，可以看成是特殊的JavaScript变量；对于结构稍微复杂一些的数据可以优先选择第二种，这种方法可以保存任意类型的数据，但需要异步调用Chrome的API，结果需要使用回调函数接收，不如第一种操作简单；第三种目前使用的不算太多，因为需要使用SQL语句对数据库进行读写操作，较前两者更加复杂，但是对于数据量庞大的应用来说是个不错的选择。开发者应根据实际的情况选择上述三种方法中的一种或几种来存储扩展中的数据。

#### Chrome存储API

Chrome为扩展应用提供了存储API，以便将扩展中需要保存的数据写入本地磁盘。Chrome提供的存储API可以说是对localStorage的改进，它与localStorage相比有以下区别：

- 如果储存区域指定为sync，数据可以自动同步；
- content_scripts可以直接读取数据，而不必通过background页面；
- 在隐身模式下仍然可以读出之前存储的数据；
- 读写速度更快；
- 用户数据可以以对象的类型保存。

## Chrome扩展的UI界面

### Browser Actions

Browser Actions将扩展图标置于Chrome浏览器工具栏中，地址栏的右侧。如果声明了popup页面，当用户点击图标时，在图标的下侧会打开这个页面。同时图标上面还可以附带badge——一个带有显示有限字符空间的区域——用以显示一些有用的信息，如未读邮件数、当前音乐播放时间等。

#### 图标

Browser Actions可以在Manifest中设定一个默认的图标，比如：

```json
"browser_action": {
    "default_icon": {
        "19": "images/icon19.png",
        "38": "images/icon38.png"
    }
}
```

一般情况下，Chrome会选择使用19像素的图片显示在工具栏中，但如果用户正在使用视网膜屏幕的计算机，则会选择38像素的图片显示。两种尺寸的图片并不是必须都指定的，如果只指定一种尺寸的图片，在另外一种环境下，Chrome会试图拉伸图片去适应，这样可能会导致图标看上去很难看。

另外，default_icon也不是必须指定的，如果没有指定，Chrome将使用一个默认图标。

通过setIcon方法可以动态更改扩展的图标，setIcon的完整方法如下：`chrome.browserAction.setIcon(details, callback)`,其中details的类型为对象，可以包含三个属性，分别是imageData、path和tabId。

imageData的值可以是imageData，也可以是对象。如果是对象，其结构为{size: imageData}，比如{'19': imageData}，这样可以单独更换指定尺寸的图片。imageData是图片的像素数据，可以通过HTML的canvas标签获取到。

path的值可以是字符串，也可以是对象。如果是对象，结构为{size: imagePath}。imagePath为图片在扩展根目录下的相对位置。

不必同时指定imageData和path，这两个属性都是指定图标所要更换的图片的。

tabId的值限定了浏览哪个标签页时，图标将被更改。

callback为回调函数，当chrome.browserAction.setIcon方法执行成功后，callback指定的函数将被运行。此函数没有可接收的回调结果。

#### Popup页面

Popup页面是当用户点击扩展图标时，展示在图标下面的页面。

Popup页面提供了一个简单便捷的UI接口。由于有时新窗口会使用户反感，而popup页面的设计更像是浏览器的一部分，看上去更加友好，但popup页面并不适用于所有情况。由于其在关闭后，就相当于用户关闭了相应的标签页，这个页面不会继续运行。当用户再次打开这个页面时，所有的DOM和js空间变量都将被重新创建。

所以，popup页面更多地是用来作为结果的展示，而不是数据的处理。通常情况下，如果需要扩展实时处理数据，而不是只在用户打开时才运行，我们需要创建一个在后端一直运行的页面或者脚本，这可以通过manifest.json的background域来声明。

#### 标题和badge

将鼠标移至扩展图标上，片刻后所显示的文字就是扩展的标题。

在Manifest中，browser\_action的default\_title属性可以设置扩展的默认标题，比如如下的例子：

```json
"browser_action": {
    "default_title": "Extension Title"
}
```

在这个扩展中，默认标题就是“Extension Title”。还可以用JavaScript来动态更改扩展的标题，方法如下：`chrome.browserAction.setTitle({title: 'This is a new title'});`

标题不仅仅只是给出扩展的名称，有时它能为用户提供更多的信息。比如一款聊天客户端的标题，可以动态地显示当前登录的帐户信息，如号码和登录状态等。所以如果能合理使用好扩展的标题，会给用户带来更好的体验。

Badge是扩展为用户提供有限信息的另外一种方法，这种方法较标题优越的地方是它可以一直显示，其缺点是只能显示大约4字节长度的信息。

### 右键菜单

当用户在网页中点击鼠标右键后，会唤出一个菜单，在上面有复制、粘贴和翻译等选项，为用户提供快捷便利的功能。Chrome也将这里开放给了开发者，也就是说我们可以把自己所编写的扩展功能放到右键菜单中。

要将扩展加入到右键菜单中，首先要在Manifest的permissions域中声明contextMenus权限。

```json
"permissions": [
    "contextMenus"
]

//同时还要在icons域声明16像素尺寸的图标，这样在右键菜单中才会显示出扩展的图标
"icons": {
    "16": "icon16.png"
}
```

右键菜单部分API说明：

```js
chrome.contextMenus.create({
    type: 'normal'， // 类型，可选：["normal", "checkbox", "radio", "separator"]，默认 normal
    title: '菜单的名字', // 显示的文字，除非为“separator”类型否则此参数必需，如果类型为“selection”，可以使用%s显示选定的文本
    contexts: ['page'], // 上下文环境，可选：["all", "page", "frame", "selection", "link", "editable", "image", "video", "audio"]，默认page
    onclick: function(){}, // 单击时触发的方法
    parentId: 1, // 右键菜单项的父菜单项ID。指定父菜单项将会使此菜单项成为父菜单项的子菜单
    documentUrlPatterns: 'https://*.baidu.com/*' // 只在某些页面显示此右键菜单
});
// 删除某一个菜单项
chrome.contextMenus.remove(menuItemId)；
// 删除所有自定义右键菜单
chrome.contextMenus.removeAll();
// 更新某一个菜单项
chrome.contextMenus.update(menuItemId, updateProperties);
```

## 零散知识点

### 获取当前窗口ID

```js
chrome.windows.getCurrent(function(currentWindow)
{
    console.log('当前窗口ID：' + currentWindow.id);
});
```

### 获取当前标签页ID

一般有2种方法：

```js
// 获取当前选项卡ID
function getCurrentTabId(callback)
{
    chrome.tabs.query({active: true, currentWindow: true}, function(tabs)
    {
        if(callback) callback(tabs.length ? tabs[0].id: null);
    });
}
```

获取当前选项卡id的另一种方法，大部分时候都类似，只有少部分时候会不一样（例如当窗口最小化时）

```js
// 获取当前选项卡ID
function getCurrentTabId2()
{
    chrome.windows.getCurrent(function(currentWindow)
    {
        chrome.tabs.query({active: true, windowId: currentWindow.id}, function(tabs)
        {
            if(callback) callback(tabs.length ? tabs[0].id: null);
        });
    });
}
```