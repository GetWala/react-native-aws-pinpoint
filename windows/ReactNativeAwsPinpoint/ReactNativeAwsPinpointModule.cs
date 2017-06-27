using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace com.getwala.ReactNativeAwsPinpoint
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class ReactNativeAwsPinpointModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="ReactNativeAwsPinpointModule"/>.
        /// </summary>
        internal ReactNativeAwsPinpointModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "ReactNativeAwsPinpoint";
            }
        }
    }
}
