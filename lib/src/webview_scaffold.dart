import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

import 'base.dart';

class WebviewScaffold extends StatefulWidget {

  final VoidCallback onTapQQ;
  final VoidCallback onTapCustomService;
  final VoidCallback onTapWeChat;
  final void Function(String uin) onWebQQ;
  final void Function(String url) onWebAlipay;

  const WebviewScaffold({
    Key key,
    this.appBar,
    @required this.url,
    this.headers,
    this.withJavascript,
    this.clearCache,
    this.clearCookies,
    this.enableAppScheme,
    this.userAgent,
    this.primary = true,
    this.persistentFooterButtons,
    this.bottomNavigationBar,
    this.withZoom,
    this.withLocalStorage,
    this.withLocalUrl,
    this.scrollBar,
    this.supportMultipleWindows,
    this.appCacheEnabled,
    this.hidden = false,
    this.initialChild,
    this.allowFileURLs,
    this.resizeToAvoidBottomInset = false,
    this.invalidUrlRegex,
    this.geolocationEnabled,
    this.onTapQQ,
    this.onTapCustomService,
    this.onTapWeChat,
    this.qq,
    this.customService,
    this.weChat,
    this.onWebQQ,
    this.onWebAlipay,
    this.weChatId,
  }) : super(key: key);

  final PreferredSizeWidget appBar;
  final String url;
  final Map<String, String> headers;
  final bool withJavascript;
  final bool clearCache;
  final bool clearCookies;
  final bool enableAppScheme;
  final String userAgent;
  final bool primary;
  final List<Widget> persistentFooterButtons;
  final Widget bottomNavigationBar;
  final bool withZoom;
  final bool withLocalStorage;
  final bool withLocalUrl;
  final bool scrollBar;
  final bool supportMultipleWindows;
  final bool appCacheEnabled;
  final bool hidden;
  final Widget initialChild;
  final bool allowFileURLs;
  final bool resizeToAvoidBottomInset;
  final String invalidUrlRegex;
  final bool geolocationEnabled;
  final bool qq;
  final bool customService;
  final bool weChat;
  final String weChatId;

  @override
  _WebviewScaffoldState createState() => _WebviewScaffoldState();
}

class _WebviewScaffoldState extends State<WebviewScaffold> {
  final webviewReference = FlutterWebviewPlugin();
  Rect _rect;
  Timer _resizeTimer;
  StreamSubscription<WebViewStateChanged> _onStateChanged;
  StreamSubscription<Null> _onTapQQ;
  StreamSubscription<Null> _onTapCustomService;
  StreamSubscription<Null> _onTapWeChat;
  StreamSubscription<String> _onWebQQ;
  StreamSubscription<String> _onWebAlipay;

  var _onBack;

  @override
  void initState() {
    super.initState();
    webviewReference.close();

    _onBack = webviewReference.onBack.listen((_) async {
      if (!mounted) return;

      // The willPop/pop pair here is equivalent to Navigator.maybePop(),
      // which is what's called from the flutter back button handler.
      final pop = await _topMostRoute.willPop();
      if (pop == RoutePopDisposition.pop) {
        // Close the webview if it's on the route at the top of the stack.
        final isOnTopMostRoute = _topMostRoute == ModalRoute.of(context);
        if (isOnTopMostRoute) {
          webviewReference.close();
        }
        Navigator.pop(context);
      }
    });

    if (widget.hidden) {
      _onStateChanged =
          webviewReference.onStateChanged.listen((WebViewStateChanged state) {
            if (state.type == WebViewState.finishLoad) {
              webviewReference.show();
            }
          });
    }

    _onTapQQ = webviewReference.onTapQQ.listen((_) {
      if (widget.onTapQQ != null)
        widget.onTapQQ();
    });

    _onTapCustomService = webviewReference.onTapCustomService.listen((_) {
      if (widget.onTapCustomService != null)
        widget.onTapCustomService();
    });

    _onTapWeChat = webviewReference.onTapWeChat.listen((_) {
      if (widget.onTapWeChat != null)
        widget.onTapWeChat();
    });

    _onWebAlipay = webviewReference.onWebAlipay.listen((url) {
      if (widget.onWebAlipay != null)
        widget.onWebAlipay(url);
    });

    _onWebQQ = webviewReference.onWebQQ.listen((uin) {
      if (widget.onWebQQ != null)
        widget.onWebQQ(uin);
    });
  }

  /// Equivalent to [Navigator.of(context)._history.last].
  Route<dynamic> get _topMostRoute {
    var topMost;
    Navigator.popUntil(context, (route) {
      topMost = route;
      return true;
    });
    return topMost;
  }

  @override
  void dispose() {
    super.dispose();
    _onBack?.cancel();
    _resizeTimer?.cancel();
    webviewReference.close();
    if (widget.hidden) {
      _onStateChanged.cancel();
    }
    _onTapQQ?.cancel();
    _onTapCustomService?.cancel();
    _onTapWeChat?.cancel();
    _onWebQQ?.cancel();
    _onWebAlipay?.cancel();
    webviewReference.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: widget.appBar,
      resizeToAvoidBottomInset: widget.resizeToAvoidBottomInset,
      persistentFooterButtons: widget.persistentFooterButtons,
      bottomNavigationBar: widget.bottomNavigationBar,
      body: _WebviewPlaceholder(
        onRectChanged: (Rect value) {
          if (_rect == null) {
            _rect = value;
            webviewReference.launch(
              widget.url,
              headers: widget.headers,
              withJavascript: widget.withJavascript,
              clearCache: widget.clearCache,
              clearCookies: widget.clearCookies,
              hidden: widget.hidden,
              enableAppScheme: widget.enableAppScheme,
              userAgent: widget.userAgent,
              rect: _rect,
              withZoom: widget.withZoom,
              withLocalStorage: widget.withLocalStorage,
              withLocalUrl: widget.withLocalUrl,
              scrollBar: widget.scrollBar,
              supportMultipleWindows: widget.supportMultipleWindows,
              appCacheEnabled: widget.appCacheEnabled,
              allowFileURLs: widget.allowFileURLs,
              invalidUrlRegex: widget.invalidUrlRegex,
              geolocationEnabled: widget.geolocationEnabled,
              qqEnabled: widget.qq,
              customServiceEnabled: widget.customService,
              weChatEnabled: widget.weChat,
              weChatId: widget.weChatId,
            );
          } else {
            if (_rect != value) {
              _rect = value;
              _resizeTimer?.cancel();
              _resizeTimer = Timer(const Duration(milliseconds: 250), () {
                // avoid resizing to fast when build is called multiple time
                webviewReference.resize(_rect);
              });
            }
          }
        },
        child: widget.initialChild ??
            const Center(child: const CircularProgressIndicator()),
      ),
    );
  }
}

class _WebviewPlaceholder extends SingleChildRenderObjectWidget {
  const _WebviewPlaceholder({
    Key key,
    @required this.onRectChanged,
    Widget child,
  }) : super(key: key, child: child);

  final ValueChanged<Rect> onRectChanged;

  @override
  RenderObject createRenderObject(BuildContext context) {
    return _WebviewPlaceholderRender(
      onRectChanged: onRectChanged,
    );
  }

  @override
  void updateRenderObject(BuildContext context,
      _WebviewPlaceholderRender renderObject) {
    renderObject..onRectChanged = onRectChanged;
  }
}

class _WebviewPlaceholderRender extends RenderProxyBox {
  _WebviewPlaceholderRender({
    RenderBox child,
    ValueChanged<Rect> onRectChanged,
  })
      : _callback = onRectChanged,
        super(child);

  ValueChanged<Rect> _callback;
  Rect _rect;

  Rect get rect => _rect;

  set onRectChanged(ValueChanged<Rect> callback) {
    if (callback != _callback) {
      _callback = callback;
      notifyRect();
    }
  }

  void notifyRect() {
    if (_callback != null && _rect != null) {
      _callback(_rect);
    }
  }

  @override
  void paint(PaintingContext context, Offset offset) {
    super.paint(context, offset);
    final rect = offset & size;
    if (_rect != rect) {
      _rect = rect;
      notifyRect();
    }
  }
}
